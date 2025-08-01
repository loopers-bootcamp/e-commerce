package com.loopers.domain.brand;

import com.loopers.annotation.ReadOnlyTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BrandService {

    private final BrandRepository brandRepository;

    @ReadOnlyTransactional
    public Optional<BrandResult.GetBrand> getBrand(Long brandId) {
        if (brandId == null) {
            return Optional.empty();
        }

        return brandRepository.findOne(brandId)
                .map(BrandResult.GetBrand::from);
    }

}
