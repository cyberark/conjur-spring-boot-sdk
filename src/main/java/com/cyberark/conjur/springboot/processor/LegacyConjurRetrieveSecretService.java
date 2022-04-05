package com.cyberark.conjur.springboot.processor;

import java.util.ArrayList;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyberark.conjur.api.Conjur;

public class LegacyConjurRetrieveSecretService {

	private static Logger logger = LoggerFactory.getLogger(LegacyConjurRetrieveSecretService.class);

	private Conjur conjur;

	public byte[] retriveMultipleSecretsForCustomAnnotation(String[] keys) throws Exception {
		conjur = new Conjur();
		String str = String.join(",", keys);
		ArrayList aList = new ArrayList(Arrays.asList(str.split(",")));
		ArrayList<String> result = new ArrayList<String>();
		for (int i = 0; i < aList.size(); i++) {
			result.add(conjur.variables().retrieveSecret((String) aList.get(i)));
		}
		return result.toString().getBytes();
	}

	public byte[] retriveSingleSecretForCustomAnnotation(String key) throws Exception {
		byte[] result = null;
		Conjur conjur = new Conjur();
		result = conjur.variables().retrieveSecret(key) != null ? conjur.variables().retrieveSecret(key).getBytes()
				: null;
		return result;
	}

}