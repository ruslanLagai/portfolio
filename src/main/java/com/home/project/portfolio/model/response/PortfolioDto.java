package com.home.project.portfolio.model.response;

import com.home.project.portfolio.model.portfolio.Account;
import com.home.project.portfolio.model.portfolio.Positions;
import lombok.Builder;
import lombok.Data;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to return back to from end
 */
@Data
public class PortfolioDto {
    private MultiValueMap<Account, Positions> positions = new LinkedMultiValueMap<>();

    public PortfolioDto addPosition(Account account, Positions positions) {
        this.positions.addIfAbsent(account, positions);
        return this;
    }

    public PortfolioDto addPositions(Account account, List<Positions> positions) {
        this.positions.addAll(account, positions);
        return this;
    }
}
