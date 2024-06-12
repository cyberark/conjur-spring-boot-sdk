package com.cyberark.conjur.springboot;

import org.springframework.boot.test.context.SpringBootTest;

import com.cyberark.conjur.springboot.annotations.ConjurValue;
import com.cyberark.conjur.springboot.annotations.ConjurValues;
import com.cyberark.conjur.springboot.processor.SpringBootConjurAutoConfiguration;

@SpringBootTest(classes = SpringBootConjurAutoConfiguration.class)
public class ConjurPluginTests {

	@ConjurValue(key = "username")
	private byte[] ddbUserNameMap;

	@ConjurValue(key = "password")
	private byte[] dbPasswordMap;

	@ConjurValues(keys = { "username", "password", "url" })
	private byte[] dbSecretsMap;

	public byte[] getDdbUserNameMap() {
		return ddbUserNameMap;
	}

	public void setDdbUserNameMap(byte[] ddbUserNameMap) {
		this.ddbUserNameMap = ddbUserNameMap;
	}

	public byte[] getDbPasswordMap() {
		return dbPasswordMap;
	}

	public void setDbPasswordMap(byte[] dbPasswordMap) {
		this.dbPasswordMap = dbPasswordMap;
	}

	public byte[] getDbSecretsMap() {
		return dbSecretsMap;
	}

	public void setDbSecretsMap(byte[] dbSecretsMap) {
		this.dbSecretsMap = dbSecretsMap;
	}
}
