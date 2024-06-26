package com.meteor.wechatbc.util;

import cn.hutool.core.text.UnicodeUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @className: XYZWUtil
 * @author: jietao.ou
 * @date: 2024/6/26 10:18
 * @version: 1.0
 */
public class XYZWUtil {

	public static void main(String[] args) {
//		String s = praiseSimpleReport("C:\\Users\\jietao.ou\\Pictures\\微信图片_20240621140906.jpg");
//		System.out.println("s = " + s);
		String text = "8226&1/100&宝箱&1/7&X396&3&8227&283.5亿HP&还差15积分领取铂金宝箱&积分值25/40&木质宝箱&抽到紫将概率5%&打开10个宝箱&X292&X41&99+&57644764/23158100a8/28&暂未激活&X396&X72&宝箱&XO";
		String t_pattern = "[X](\\d+)";
		Pattern brick_p = Pattern.compile(t_pattern);
		Matcher brick_m = brick_p.matcher(text);
		StringBuilder builder = new StringBuilder();
		int sumPoint = 0;
		int i = 0;
		while (brick_m.find()) {
			if (i < 5){
				Title1 byCode = Title1.findByCode(i);
				i++;
				if (ObjUtil.isEmpty(byCode)){
					continue;
				}
				Integer count = Integer.valueOf(brick_m.group(1));
				int point = count * byCode.getPoint();
				builder.append(String.format("%s：%s,折算：%s 分", byCode.name, count, point)).append("\n");
				sumPoint += point;
			}
		}
		System.out.println(builder);
	}

	public static String praiseSimpleReport(String url) {
		String base64Image = readPic(url);
		OCRApiResult apiResult = ocrHandler(base64Image);
		if (100 == apiResult.getCode()) {
			String text = Optional.ofNullable(apiResult.getData()).orElse(new ArrayList<>())
					.stream().map(ApiData::getText).collect(Collectors.joining("&"));
			return dataHandler(text);
		}
		return null;
	}

	public static String praiseSimpleReport(BufferedImage bufferedImage) {
		// 将BufferedImage对象写入到ByteArrayOutputStream中
		// 将BufferedImage对象写入到ByteArrayOutputStream中
		String base64Image = null;
		try {
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			ImageIO.write(bufferedImage, "jpg", byteArrayOutputStream);
			// 使用Base64编码器将字节数组转换为Base64编码的字符串
			byte[] imageBytes = byteArrayOutputStream.toByteArray();
			base64Image = Base64.getEncoder().encodeToString(imageBytes);
		} catch (IOException e) {
			return null;
		}

		OCRApiResult apiResult = ocrHandler(base64Image);
		if (100 == apiResult.getCode()) {
			String text = Optional.ofNullable(apiResult.getData()).orElse(new ArrayList<>())
					.stream().map(ApiData::getText).collect(Collectors.joining("&"));
			return dataHandler(text);
		}
		return null;
	}

	private static String dataHandler(String text) {
		if (StrUtil.isEmpty(text)) {
			return null;
		}
		if (text.contains("咸鱼简报")) {
			String coin_pattern = "金币[×x](\\d+\\.?\\d*)";
			String t_pattern = "[×x](\\d+)";


			Pattern coin_p = Pattern.compile(coin_pattern);
			Matcher coin_m = coin_p.matcher(text);
			if (coin_m.find()) {
				double coin_number = Double.parseDouble(coin_m.group(1));
				System.out.println("金币数量：" + coin_number);
			} else {
				System.out.println("未找到金币数量");
			}

			StringBuilder builder = new StringBuilder();
			int sumPoint = 0;
			for (title s : title.values()) {
				Pattern brick_p = Pattern.compile(s.name + t_pattern);
				Matcher brick_m = brick_p.matcher(text);
				if (brick_m.find()) {
					int brick_number = Integer.parseInt(brick_m.group(1));
					switch (s) {
						case T1:
							builder.append(String.format("%s：%s,目标：25万，缺：%s", s.name, brick_number, NumberUtil.max(250000 - brick_number, 0))).append("\n");
							break;
						case T2:
							builder.append(String.format("%s：%s,目标：3200，缺：%s", s.name, brick_number, NumberUtil.max(3200 - brick_number, 0))).append("\n");
							break;
						case T3:
							builder.append(String.format("%s：%s,目标：750，缺：%s", s.name, brick_number, NumberUtil.max(750 - brick_number, 0))).append("\n");
							break;
						case T4:
							builder.append(String.format("%s：%s,折算：%s 分", s.name, brick_number, brick_number * s.point)).append("\n");
							break;
						case T5:
							builder.append(String.format("%s：%s,折算：%s 分", s.name, brick_number, s.point * brick_number)).append("\n");

							break;
						case T6:
							builder.append(String.format("%s：%s,折算：%s 分", s.name, brick_number, s.point * brick_number)).append("\n");
							break;
						case T7:
							builder.append(String.format("%s：%s,折算：%s 分", s.name, brick_number, s.point * brick_number)).append("\n");
							break;
						case T8:
							builder.append(String.format("未领取积分：%s 分", brick_number)).append("\n");
							break;
					}
					sumPoint += brick_number * s.point;
//				System.out.println(String.format("%s数量：%s",s.name,brick_number));
				} else {
					System.out.println(String.format("未找到%s数量", s.name));
				}
			}
			builder.append(String.format("宝箱周：%s轮", BigDecimal.valueOf(NumberUtil.div(sumPoint, 3400)).setScale(2, RoundingMode.DOWN))).append("\n");
			builder.append(String.format("积分汇总：%s,目标：32000，缺：%s", sumPoint, NumberUtil.max(32000 - sumPoint, 0))).append("\n");
			return builder.toString();
		} else if (text.contains("宝箱") && text.contains("打开") && text.contains("领取")) {
			String t_pattern = "[X](\\d+)";
			Pattern brick_p = Pattern.compile(t_pattern);
			Matcher brick_m = brick_p.matcher(text);
			StringBuilder builder = new StringBuilder();
			int sumPoint = 0;
			int i = 0;
			while (brick_m.find()) {
				if (i < 5){
					Title1 byCode = Title1.findByCode(i);
					i++;
					if (ObjUtil.isEmpty(byCode)){
						continue;
					}
					Integer count = Integer.valueOf(brick_m.group(1));
					int point = count * byCode.getPoint();
					builder.append(String.format("%s：%s,折算：%s 分", byCode.name, count, point)).append("\n");
					sumPoint += point;
				}
			}
			String t_pattern1 = "[积分值](\\d+)";
			Pattern brick_p1 = Pattern.compile(t_pattern1);
			Matcher brick_m1 = brick_p1.matcher(text);

			if (brick_m1.find()) {
				Integer count = Integer.valueOf(brick_m1.group(1));
				builder.append(String.format("未领取积分：%s 分", count)).append("\n");
				sumPoint += count;
			}
			builder.append(String.format("原始积分：%s", sumPoint)).append("\n");
			builder.append(String.format("宝箱周：%s轮", BigDecimal.valueOf(NumberUtil.div(sumPoint, 3400)).setScale(2,RoundingMode.DOWN))).append("\n");
			return builder.toString();
		}
		return null;
	}

	/**
	 * umi-ocr 
	 * @see https://github.com/hiroi-sora/Umi-OCR
	 * @param base64Image
	 * @return
	 */
	private static OCRApiResult ocrHandler(String base64Image) {
		String url = "http://127.0.0.1:1224/api/ocr";
		Map<String, Object> params = new HashMap<>();
		Map options = new HashMap<>();
		options.put("data.format", "json");
		options.put("tbpu.parser", "single_none");
		params.put("base64", base64Image);
		params.put("options ", options);
		String post = HttpUtil.post(url, JSON.toJSONString(params));
		String result = UnicodeUtil.toString(post);
		System.out.println(result);
		OCRApiResult apiResult = JSON.parseObject(result, OCRApiResult.class);
		return apiResult;
	}

	private static String readPic(String url) {
		File file = new File(url); // 替换为实际图片路径
		String base64Image = "";
		try {
			FileInputStream fileInputStream = new FileInputStream(file);
			byte[] imageBytes = new byte[(int) file.length()];
			fileInputStream.read(imageBytes);
			base64Image = Base64.getEncoder().encodeToString(imageBytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return base64Image;
	}

	public static class OCRApiResult {
		private Integer code;
		private List<ApiData> data;
		private Double score;
		private Double time;
		private Double timestamp;

		public Integer getCode() {
			return code;
		}

		public void setCode(Integer code) {
			this.code = code;
		}

		public List<ApiData> getData() {
			return data;
		}

		public void setData(List<ApiData> data) {
			this.data = data;
		}

		public Double getScore() {
			return score;
		}

		public void setScore(Double score) {
			this.score = score;
		}

		public Double getTime() {
			return time;
		}

		public void setTime(Double time) {
			this.time = time;
		}

		public Double getTimestamp() {
			return timestamp;
		}

		public void setTimestamp(Double timestamp) {
			this.timestamp = timestamp;
		}
	}

	public static class ApiData {
		private Object box;
		private Double score;
		private String text;
		private String end;

		public Object getBox() {
			return box;
		}

		public void setBox(Object box) {
			this.box = box;
		}

		public Double getScore() {
			return score;
		}

		public void setScore(Double score) {
			this.score = score;
		}

		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
		}

		public String getEnd() {
			return end;
		}

		public void setEnd(String end) {
			this.end = end;
		}
	}

	public enum Title1 {
		T4(0, "木质宝箱", 1),
		T5(1, "青铜宝箱", 10),
		T6(2, "黄金宝箱", 20),
		T7(4, "铂金宝箱", 50);
		private String name;
		private Integer code;
		private Integer point;

		public static Title1 findByCode(Integer code){
			for (Title1 value : Title1.values()) {
				if (value.code == code){
					return value;
				}
			}
			return null;
		}
		
		Title1(Integer code, String name, Integer point) {
			this.name = name;
			this.code = code;
			this.point = point;
		}

		public Integer getPoint() {
			return point;
		}

		public void setPoint(Integer point) {
			this.point = point;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Integer getCode() {
			return code;
		}

		public void setCode(Integer code) {
			this.code = code;
		}
	}

	public enum title {
		T1("金砖", 0),
		T2("招募令", 0),
		T3("黄金鱼竿", 0),
		T4("木质宝箱", 1),
		T5("青铜宝箱", 10),
		T6("黄金宝箱", 20),
		T7("铂金宝箱", 50),
		T8("宝箱积分", 1),
		;
		private String name;
		private Integer point;

		title(String name, Integer value) {
			this.name = name;
			this.point = value;
		}


		public String getName() {
			return name;
		}


		public Integer getPoint() {
			return point;
		}

	}
}
