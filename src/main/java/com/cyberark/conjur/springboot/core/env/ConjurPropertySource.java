package com.cyberark.conjur.springboot.core.env;

import java.util.Objects;

import org.springframework.core.env.EnumerablePropertySource;

import com.cyberark.conjur.api.Conjur;

/**
 * 
 * This class resolves the secret value for give vault path at application load
 * time from the conjur vault.
 *
 */
public class ConjurPropertySource extends EnumerablePropertySource<Object> {

	private String vaultInfo = "";

	private String vaultPath = "";

	private Conjur conjur;

	protected ConjurPropertySource(String vaultPath) {
		super(vaultPath + "@");
		this.vaultPath = vaultPath;

	}

	protected ConjurPropertySource(String vaultPath, String vaultInfo) {
		super(vaultPath + "@" + vaultInfo);
		this.vaultPath = vaultPath;
		this.vaultInfo = vaultInfo;
	}

	@Override
	public String[] getPropertyNames() {
		return new String[0];
	}

	/**
	 * Method which resolves @value annotation queries and return result in the form
	 * of byte array.
	 */

	@Override
	public Object getProperty(String key) {

		key = ConjurConfig.getInstance().mapProperty(key);

		ConjurConnectionManager.getInstance();
		if (null == conjur) {
			conjur = new Conjur();
		}
		byte[] result = null;
		try {
			result = conjur.variables().retrieveSecret(vaultPath + key) != null
					? conjur.variables().retrieveSecret(vaultPath + key).getBytes()
					: null;
		} catch (Exception e) {
			//
		}
		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(conjur, vaultInfo, vaultPath);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ConjurPropertySource other = (ConjurPropertySource) obj;
		return Objects.equals(conjur, other.conjur) && Objects.equals(vaultInfo, other.vaultInfo)
				&& Objects.equals(vaultPath, other.vaultPath);
	}

}