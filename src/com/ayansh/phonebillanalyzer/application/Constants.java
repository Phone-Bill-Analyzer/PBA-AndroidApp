/**
 * 
 */
package com.ayansh.phonebillanalyzer.application;

/**
 * @author varun
 *
 */
public class Constants {

	private static boolean premiumVersion;
	private static String productTitle, productDescription, productPrice;
	
	public static String getPublicKey() {
		return "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQ" +
				"EAvBxmhNvSTAsBy8oJFx74Zh7mIbfq79q5Sa+EFtv" +
				"Ir9pe1KzO06bztHQvNbn5f4mca2E6b25MrD7+2pGj" +
				"+wr2A7qk92XxdctCY8D+gZ7znFtwPwxbUYBTf5LoD" +
				"RnLY61UIJSLtXsq0gt8G05Kxva9Sx7eBLJXtf9fLs" +
				"qH/CAUk1JrER4266gUm3KQK0hNVEYnelH08FXX3jM" +
				"uQq6Zf2QAXUpcU7y9E4bGlPo4j494YX8P1TxvGl6V" +
				"dF9nX8RdnDQmsR1kkjGrfVf9d3GRbzjffe0bg9Cj+" +
				"27cV6K8fiyuupWxasr9yjQxFA7FWA2xFa57PtRljm" +
				"U+1oOMc+KaKfxxHwIDAQAB";
	}

	public static String getProductKey() {
		return "premium_content";
	}

	public static void setPremiumVersion(boolean premiumVersion) {
		Constants.premiumVersion = premiumVersion;
	}
	
	public static boolean isPremiumVersion(){
		return premiumVersion;
	}

	/**
	 * @return the productTitle
	 */
	public static String getProductTitle() {
		return productTitle;
	}

	/**
	 * @param productTitle the productTitle to set
	 */
	public static void setProductTitle(String productTitle) {
		Constants.productTitle = productTitle;
	}

	/**
	 * @return the productDescription
	 */
	public static String getProductDescription() {
		return productDescription;
	}

	/**
	 * @param productDescription the productDescription to set
	 */
	public static void setProductDescription(String productDescription) {
		Constants.productDescription = productDescription;
	}

	/**
	 * @return the productPrice
	 */
	public static String getProductPrice() {
		return productPrice;
	}

	/**
	 * @param productPrice the productPrice to set
	 */
	public static void setProductPrice(String productPrice) {
		Constants.productPrice = productPrice;
	}

}