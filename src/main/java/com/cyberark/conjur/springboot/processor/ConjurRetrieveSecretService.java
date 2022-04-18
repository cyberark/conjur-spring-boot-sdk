package com.cyberark.conjur.springboot.processor;

import java.util.ArrayList;
import java.util.Arrays;

import com.cyberark.conjur.api.Conjur;

public class ConjurRetrieveSecretService {

	private Conjur conjur;

	/**
	 * This method retrieves multiple secrets for custom annotation's keys.
	 * 
	 * @param keys - query to vault.
	 * @return secrets - output from the conjur vault in form of byte array.
	 */
	public byte[] retriveMultipleSecretsForCustomAnnotation(String[] keys){
		conjur = new Conjur();
		String str = String.join(",", keys);
		ArrayList<String> aList = new ArrayList<>(Arrays.asList(str.split(",")));
		ArrayList<String> result = new ArrayList<>();
		try {
			for (int i = 0; i < aList.size(); i++) {
				result.add(conjur.variables().retrieveSecret(aList.get(i)));
			}
		} catch (Exception e) {
			e.getMessage();
		}
		return result.toString().getBytes();
	}

	/**
	 * This method retrieves single secret for custom annotation's key value.
	 * 
	 * @param key - query to vault.
	 * @return secrets - output from the vault in form of byte array.
	 */
	public byte[] retriveSingleSecretForCustomAnnotation(String key) {
		byte[] result = null;
		conjur = new Conjur();
		try {
			result = conjur.variables().retrieveSecret(key) != null ? conjur.variables().retrieveSecret(key).getBytes()
					: null;
		} catch (Exception e) {
			e.getMessage();
		}
		return result;
	}

}