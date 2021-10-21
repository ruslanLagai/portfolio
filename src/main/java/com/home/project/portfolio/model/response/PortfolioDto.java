package com.home.project.portfolio.model.response;

import com.home.project.portfolio.model.operations.Overbook;
import com.home.project.portfolio.model.portfolio.Account;
import com.home.project.portfolio.model.portfolio.Positions;
import lombok.Data;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.*;

/**
 * Class to return back to from end
 */
@Data
public class PortfolioDto {
    private MultiValueMap<Account, Positions> positions = new LinkedMultiValueMap<>();
    private MultiValueMap<String, Overbook> prices = new LinkedMultiValueMap<>();

    public PortfolioDto addPosition(Account account, Positions positions) {
        this.positions.addIfAbsent(account, positions);
        return this;
    }

    public PortfolioDto addPositions(Account account, List<Positions> positions) {
        this.positions.addAll(account, positions);
        return this;
    }

    public PortfolioDto addPrice(String figi, Overbook overbook) {
        this.prices.putIfAbsent(figi, Collections.singletonList(overbook));
        return this;
    }
}
