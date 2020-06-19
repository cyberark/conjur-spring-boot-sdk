package com.db.cso.conjur.authn;

import com.db.cso.conjur.domain.DapConfig;
import com.db.cso.conjur.exception.AuthenticationException;

public interface ConjurAuthenticator {
    String authenticate(DapConfig dapConfig) throws AuthenticationException;
}
