package com.home.project.portfolio.mapper;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tinkoff.piapi.contract.v1.AccessLevel;
import ru.tinkoff.piapi.contract.v1.Account;
import ru.tinkoff.piapi.contract.v1.AccountStatus;
import ru.tinkoff.piapi.contract.v1.AccountType;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author rlagay
 */
@ExtendWith(MockitoExtension.class)
class AccountMapperTest {

    private static final String NAME = "name";
    private final AccountMapper mapper = Mappers.getMapper(AccountMapper.class);

    @ParameterizedTest
    @MethodSource("getParams")
    void mapAccount(AccountType accountType, AccountStatus accountStatus,
                    com.home.project.portfolio.model.portfolio.Account.AccountType mappedType) {
        var result = mapper.mapAccount(account(accountType, accountStatus));
        assertAll(() -> {
            assertEquals("1", result.getBrokerAccountId());
            assertEquals(NAME, result.getName());
            assertEquals(mappedType, result.getBrokerAccountType());
            assertEquals(accountStatus.name(), result.getStatus());

        });
    }

    private static Stream<Arguments> getParams() {
        return Stream.of(
            Arguments.of(AccountType.ACCOUNT_TYPE_TINKOFF, AccountStatus.ACCOUNT_STATUS_CLOSED,
                com.home.project.portfolio.model.portfolio.Account.AccountType.Tinkoff),
            Arguments.of(AccountType.ACCOUNT_TYPE_TINKOFF_IIS, AccountStatus.ACCOUNT_STATUS_NEW,
                com.home.project.portfolio.model.portfolio.Account.AccountType.TinkoffIis),
            Arguments.of(AccountType.ACCOUNT_TYPE_INVEST_BOX, AccountStatus.ACCOUNT_STATUS_OPEN,
                com.home.project.portfolio.model.portfolio.Account.AccountType.InvestBox),
            Arguments.of(AccountType.ACCOUNT_TYPE_UNSPECIFIED, AccountStatus.ACCOUNT_STATUS_CLOSED,
                com.home.project.portfolio.model.portfolio.Account.AccountType.Unspecified)
        );
    }

    private Account account(AccountType accountType, AccountStatus accountStatus) {
        return Account.newBuilder()
            .setType(accountType)
            .setAccessLevel(AccessLevel.ACCOUNT_ACCESS_LEVEL_FULL_ACCESS)
            .setName(NAME)
            .setId("1")
            .setStatus(accountStatus)
            .build();
    }
}