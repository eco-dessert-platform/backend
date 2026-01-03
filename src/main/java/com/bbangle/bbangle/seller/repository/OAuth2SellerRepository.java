package com.bbangle.bbangle.seller.repository;

import com.bbangle.bbangle.auth.oauth.OauthServerType;
import com.bbangle.bbangle.seller.domain.OAuth2Seller;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

// TODO : Test
public interface OAuth2SellerRepository extends JpaRepository<OAuth2Seller, Long> {

    Optional<OAuth2Seller> findByProviderAndProviderId(OauthServerType provider, String providerId);
}
