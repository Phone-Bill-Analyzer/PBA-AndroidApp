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
		return "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAoEx5Kz"
				+ "9Q0ZXQSXXhjmS1HROI6VpT1jMMbRI1P2tuHqDNNWQZYVCP4"
				+ "Xz3G7zfbjkZ8i8zNhvWkf89TSwb+VRVrNuNrarasftjhbyG"
				+ "iW8lv3eTIP4NGoyEP2wIoxuX0NbeCvtZjHIgwICdw9H56dN"
				+ "5YtMEyKWa2TJiLAkbiOK4qFlKzUy5OMQBJ+kg8Khh8zpw/o"
				+ "AozsKTehz9crhsSQDJNOls41yiOfys1VhXZfIYFFBQdibgom"
				+ "h4RvakSqbyxh1SSGbGxO71I0FOozBQ9G8BLWrDoUa3oZgLjJ"
				+ "eszWOc/vZdqDjbFJoHNpOfKKAmUwO8uHK6j+NSF+jpLqHg0M"
				+ "dO7wIDAQAB";
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