package com.home.project.portfolio.mapper;

import com.home.project.portfolio.model.portfolio.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import ru.tinkoff.piapi.contract.v1.AccountStatus;
import ru.tinkoff.piapi.contract.v1.AccountType;

import java.util.Map;

import static ru.tinkoff.piapi.contract.v1.AccountType.ACCOUNT_TYPE_INVEST_BOX;
import static ru.tinkoff.piapi.contract.v1.AccountType.ACCOUNT_TYPE_TINKOFF;
import static ru.tinkoff.piapi.contract.v1.AccountType.ACCOUNT_TYPE_TINKOFF_IIS;
import static ru.tinkoff.piapi.contract.v1.AccountType.ACCOUNT_TYPE_UNSPECIFIED;

/**
 * @author rlagay
 */
@Mapper(componentModel = "spring")
public interface AccountMapper {

    Map<AccountType, Account.AccountType> ACCOUNT_TYPE_MAP = Map.of(
        ACCOUNT_TYPE_UNSPECIFIED, Account.AccountType.Unspecified,
        ACCOUNT_TYPE_TINKOFF, Account.AccountType.Tinkoff,
        ACCOUNT_TYPE_TINKOFF_IIS, Account.AccountType.TinkoffIis,
        ACCOUNT_TYPE_INVEST_BOX, Account.AccountType.InvestBox
    );

    @Mappings({
        @Mapping(target = "brokerAccountType", source = "type", qualifiedByName = "getType"),
        @Mapping(target = "brokerAccountId", source = "id"),
        @Mapping(target = "status", source = "status", qualifiedByName = "getStatus")
    })
    Account mapAccount(ru.tinkoff.piapi.contract.v1.Account account);

    @Named("getType")
    default Account.AccountType getType(AccountType type) {
        return ACCOUNT_TYPE_MAP.get(type);
    }

    @Named("getStatus")
    default String getStatus(AccountStatus status) {
        return status.name();
    }
}
