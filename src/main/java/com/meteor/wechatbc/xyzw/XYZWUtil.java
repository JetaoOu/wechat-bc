package com.meteor.wechatbc.xyzw;

import cn.hutool.core.text.UnicodeUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import lombok.Data;
import lombok.Getter;

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
	
	private static final BigDecimal pointSku = BigDecimal.valueOf(3460);

	public static void main(String[] args) {
		String s = praiseSimpleReport("C:\\Users\\jietao.ou\\Pictures\\2bc0e4d1661979bf1aa670a52bc4b9d.jpg");
		System.out.println(s);
	}

	public static String praiseSimpleReport(String url) {
		String base64Image = readPic(url);
		OCRApiResult apiResult = ocrHandler(base64Image);
		if (100 == apiResult.getCode()) {
			return dataHandler(apiResult);
		}
		return null;
	}

	//宝箱图
	public static String praisePointBox(String url) {
		String base64Image = readPic(url);
		OCRApiResult apiResult = ocrHandler(base64Image);
		return XYZWSimpleReportDTO.buildBox(apiResult).printBoxResult();
	}

	/**
	 * 咸鱼简报
	 *
	 * @param bufferedImage
	 * @return
	 */
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
			return dataHandler(apiResult);
		}
		return null;
	}

	private static String dataHandler(OCRApiResult apiResult) {
		String text = Optional.ofNullable(apiResult.getData()).orElse(new ArrayList<>())
				.stream().map(ApiData::getText).collect(Collectors.joining("&"));
		if (StrUtil.isEmpty(text)) {
			return null;
		}
		if (text.contains("咸鱼简报") && !text.contains("帮助")) {
			return XYZWSimpleReportDTO.buildSimpleReport(text).printSimpleReportResult();
		} else if (text.contains("宝箱") && text.contains("打开") && text.contains("领取")) {
			return XYZWSimpleReportDTO.buildBox(apiResult).printBoxResult();
		}
		return null;
	}

	/**
	 * umi-ocr
	 *
	 * @param base64Image
	 * @return
	 * @see <a href="https://github.com/hiroi-sora/Umi-OCR">Umi-OCR</a>
	 */
	private static OCRApiResult ocrHandler(String base64Image) {
		String url = "http://127.0.0.1:1224/api/ocr";
		Map<String, Object> params = new HashMap<>();
		Map options = new HashMap<>();
		options.put("data.format", "dict");
		options.put("tbpu.parser", "single_none");
		params.put("base64", base64Image);
		params.put("options ", options);
		String post = HttpUtil.post(url, JSON.toJSONString(params));
		String result = UnicodeUtil.toString(post);
//		System.out.println(result);
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

	@Data
	public static class OCRApiResult {
		private Integer code;
		private List<ApiData> data;
		private Double score;
		private Double time;
		private Double timestamp;

	}

	@Data
	public static class ApiData {
		private List<List<Integer>> box;
		private Double score;
		private String text;
		private String end;

	}

	@Getter
	public enum Title1 {
		T4(0, "木质宝箱", 1),
		T5(1, "青铜宝箱", 10),
		T6(2, "黄金宝箱", 20),
		T7(4, "铂金宝箱", 50);
		private String name;
		private Integer code;
		private Integer point;

		public static Title1 findByCode(Integer code) {
			for (Title1 value : Title1.values()) {
				if (value.code == code) {
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

	}

	@Getter
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

	}

	@Data
	public static class XYZWSimpleReportDTO {
		/**
		 * 金币
		 */
		private BigDecimal coin;

		/**
		 * 金砖
		 */
		private Integer gold;

		/**
		 * 招募令
		 */
		private Integer recruit;

		/**
		 * 金鱼杆
		 */
		private Integer goldFish;

		/**
		 * 木箱
		 */
		private Integer woodBox;


		/**
		 * 青铜箱
		 */
		private Integer bronzeBox;

		/**
		 * 黄金箱
		 */
		private Integer goldBox;
		/**
		 * 铂金盒子
		 */
		private Integer platinumBox;

		/**
		 * 积分余额
		 */
		private Integer pointBalance;

		public BigDecimal getCoin() {
			return Optional.ofNullable(coin).orElse(BigDecimal.ZERO);
		}

		public Integer getGold() {
			return Optional.ofNullable(gold).orElse(0);
		}

		public Integer getRecruit() {
			return Optional.ofNullable(recruit).orElse(0);
		}

		public Integer getGoldFish() {
			return Optional.ofNullable(goldFish).orElse(0);
		}

		public Integer getWoodBox() {
			return Optional.ofNullable(woodBox).orElse(0);
		}

		public Integer getBronzeBox() {
			return Optional.ofNullable(bronzeBox).orElse(0);
		}

		public Integer getGoldBox() {
			return Optional.ofNullable(goldBox).orElse(0);
		}

		public Integer getPlatinumBox() {
			return Optional.ofNullable(platinumBox).orElse(0);
		}

		public Integer getPointBalance() {
			return Optional.ofNullable(pointBalance).orElse(0);
		}

		public BigDecimal sumPoint() {
			return NumberUtil.add(
					NumberUtil.mul(getWoodBox(), title.T4.getPoint()),
					NumberUtil.mul(getBronzeBox(), title.T5.getPoint()),
					NumberUtil.mul(getGoldBox(), title.T6.getPoint()),
					NumberUtil.mul(getPlatinumBox(), title.T7.getPoint()),
					pointBalance
			);
		}
		public String printSimpleReportResult() {
			StringBuilder builder = new StringBuilder();
			builder.append(String.format("%s：%s,目标：25万，缺：%s", title.T1.name, getGold(), NumberUtil.max(250000 - getGold(), 0))).append("\n");
			builder.append(String.format("%s：%s,目标：3200，缺：%s", title.T2.name, getRecruit(), NumberUtil.max(3200 - getRecruit(), 0))).append("\n");
			builder.append(String.format("%s：%s,目标：750，缺：%s", title.T3.name, getGoldFish(), NumberUtil.max(750 - getGoldFish(), 0))).append("\n");
			builder.append(String.format("%s：%s,折算：%s 分", title.T4.name, getWoodBox(), NumberUtil.mul(getWoodBox(), title.T4.getPoint()))).append("\n");
			builder.append(String.format("%s：%s,折算：%s 分", title.T5.name, getBronzeBox(), NumberUtil.mul(getBronzeBox(), title.T5.getPoint()))).append("\n");
			builder.append(String.format("%s：%s,折算：%s 分", title.T6.name, getGoldBox(), NumberUtil.mul(getGoldBox(), title.T6.getPoint()))).append("\n");
			builder.append(String.format("%s：%s,折算：%s 分", title.T7.name, getPlatinumBox(), NumberUtil.mul(getPlatinumBox(), title.T7.getPoint()))).append("\n");
			builder.append(String.format("未领取积分：%s 分", getPointBalance())).append("\n");
			builder.append(String.format("宝箱周：%s 轮", NumberUtil.div(sumPoint(), pointSku, 2))).append("\n");
			builder.append(String.format("积分汇总：%s,目标：32000，缺：%s", sumPoint(), NumberUtil.max(NumberUtil.sub(32000 , sumPoint()), BigDecimal.ZERO))).append("\n");
			return builder.toString();
		}

		public String printBoxResult() {
			StringBuilder builder = new StringBuilder();
			builder.append(String.format("%s：%s,折算：%s 分", title.T4.name, getWoodBox(), NumberUtil.mul(getWoodBox(), title.T4.getPoint()))).append("\n");
			builder.append(String.format("%s：%s,折算：%s 分", title.T5.name, getBronzeBox(), NumberUtil.mul(getBronzeBox(), title.T5.getPoint()))).append("\n");
			builder.append(String.format("%s：%s,折算：%s 分", title.T6.name, getGoldBox(), NumberUtil.mul(getGoldBox(), title.T6.getPoint()))).append("\n");
			builder.append(String.format("%s：%s,折算：%s 分", title.T7.name, getPlatinumBox(), NumberUtil.mul(getPlatinumBox(), title.T7.getPoint()))).append("\n");
			builder.append(String.format("未领取积分：%s 分", getPointBalance())).append("\n");
			builder.append(String.format("原始积分：%s 分", NumberUtil.sub(sumPoint(), getPointBalance()))).append("\n");
			builder.append(String.format("宝箱周：%s 轮", NumberUtil.div(sumPoint(), pointSku, 2))).append("\n");
			builder.append(String.format("不开木箱：%s 轮", NumberUtil.div(sumPoint().subtract(NumberUtil.mul(getWoodBox(), title.T4.getPoint())), pointSku, 2))).append("\n");
			builder.append(String.format("不开铂金：%s 轮", NumberUtil.div(sumPoint().subtract(NumberUtil.mul(getPlatinumBox(), title.T7.getPoint())), pointSku, 2))).append("\n");
			return builder.toString();
		}

		public static XYZWSimpleReportDTO buildSimpleReport(String text) {

			XYZWSimpleReportDTO dto = new XYZWSimpleReportDTO();

			String coin_pattern = "金币[×x](\\d+\\.?\\d*)";
			String t_pattern = "[×x](\\d*\\.\\d+|\\d+)";


			Pattern coin_p = Pattern.compile(coin_pattern);
			Matcher coin_m = coin_p.matcher(text);
			if (coin_m.find()) {
				BigDecimal bigDecimal = new BigDecimal(coin_m.group(1));
				dto.setCoin(bigDecimal);
			} else {
				System.out.println("未找到金币数量");
			}

			for (title s : title.values()) {
				Pattern brick_p = Pattern.compile(s.name + t_pattern);
				Matcher brick_m = brick_p.matcher(text);
				if (brick_m.find()) {
					String group = brick_m.group(1);
					int brick_number = 0;
					if (hasDecimalValue(group)){
						BigDecimal bigDecimal = new BigDecimal(group);
						BigDecimal bigDecimal1 = NumberUtil.mul(bigDecimal, BigDecimal.valueOf(10000));
						brick_number = bigDecimal1.intValue();
					}else{
						brick_number = Integer.parseInt(group);
					}
					switch (s) {
						case T1:
							dto.setGold(brick_number);
							break;
						case T2:
							dto.setRecruit(brick_number);
							break;
						case T3:
							dto.setGoldFish(brick_number);
							break;
						case T4:
							dto.setWoodBox(brick_number);
							break;
						case T5:
							dto.setBronzeBox(brick_number);
							break;
						case T6:
							dto.setGoldBox(brick_number);
							break;
						case T7:
							dto.setPlatinumBox(brick_number);
							break;
						case T8:
							dto.setPointBalance(brick_number);
							break;
					}
//				System.out.println(String.format("%s数量：%s",s.name,brick_number));
				} else {
					System.out.println(String.format("未找到%s数量", s.name));
				}
			}
			return dto;
		}
		public static boolean hasDecimalValue(String numStr) {
			int decimalIndex = numStr.indexOf('.');
			if (decimalIndex == -1) {
				return false; // 没有小数点
			}
			for (int i = decimalIndex + 1; i < numStr.length(); i++) {
				if (numStr.charAt(i) != '0') {
					return true; // 小数点后有非零值
				}
			}
			return false; // 小数点后全为零
		}
		public static XYZWSimpleReportDTO buildBox(OCRApiResult apiResult) {
			List<ApiData> data = apiResult.getData();
			List<ApiData> collect = data.stream().filter(d -> d.getText().startsWith("X")||d.getText().startsWith("x")).collect(Collectors.toList());
			List<ApiData> dataList = collect.stream().sorted(Comparator.comparingInt(d -> d.getBox().get(0).get(0)))
					.collect(Collectors.toList());
			if (ObjUtil.isEmpty(dataList) || 4 == dataList.size()) {
				return null;
			}
			XYZWSimpleReportDTO dto = new XYZWSimpleReportDTO();
			dto.setWoodBox(Integer.valueOf(dataList.get(0).getText().replace("x", "").replace("X", "").trim()));
			dto.setBronzeBox(Integer.valueOf(dataList.get(1).getText().replace("x", "").replace("X", "").trim()));
			dto.setGoldBox(Integer.valueOf(dataList.get(2).getText().replace("x", "").replace("X", "").trim()));
			dto.setPlatinumBox(Integer.valueOf(dataList.get(4).getText().replace("x", "").replace("X", "").trim()));
			Optional<String> optional = data.stream().filter(d -> d.getText().startsWith("积分值")).findFirst().map(ApiData::getText);
			if (optional.isPresent()) {
				String t_pattern1 = "[积分值](\\d+)";
				Pattern brick_p1 = Pattern.compile(t_pattern1);
				Matcher brick_m1 = brick_p1.matcher(optional.get());

				if (brick_m1.find()) {
					Integer count = Integer.valueOf(brick_m1.group(1));
					dto.setPointBalance(count);
				}
			}
			return dto;
		}
	}
}
