package com.loopers.domain.brand;

import java.util.Optional;

public interface BrandRepository {

    Optional<Brand> findOne(Long brandId);

}
