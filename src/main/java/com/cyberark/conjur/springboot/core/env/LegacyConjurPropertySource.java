package com.cyberark.conjur.springboot.core.env;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.EnumerablePropertySource;

import com.cyberark.conjur.api.Conjur;

/**
 * 
 * This class resolves the secret value for give vault path at application load
 * time from the conjur vault.
 *
 */
public class LegacyConjurPropertySource
//extends PropertySource<Object> {
//consider the following alternative if miss rates are excessive
		extends EnumerablePropertySource<Object> {

	private String vaultInfo = "";

	private String vaultPath = "";

	private Conjur conjur;

	private static Logger logger = LoggerFactory.getLogger(LegacyConjurPropertySource.class);

	protected LegacyConjurPropertySource(String vaultPath) {
		super(vaultPath + "@");
		this.vaultPath = vaultPath;

	}

	protected LegacyConjurPropertySource(String vaultPath, String vaultInfo) {
		super(vaultPath + "@" + vaultInfo);
		this.vaultPath = vaultPath;
		this.vaultInfo = vaultInfo;
	}

	@Override
	public String[] getPropertyNames() {
		return new String[0];
	}

	/**
	 * Method which resolves @value annotation queries.
	 */

	@Override
	public Object getProperty(String key) {

		key = ConjurConfig.getInstance().mapProperty(key);

		LegacyConjurConnectionManager.getInstance();
		if (null == conjur) {
			conjur = new Conjur();
		}
		byte[] result = null;
		try {
			result = conjur.variables().retrieveSecret(vaultPath + key) != null
					? conjur.variables().retrieveSecret(vaultPath + key).getBytes()
					: null;
			
		} catch (Exception ae) {
			ae.getMessage();
		}
		return result;
	}

}