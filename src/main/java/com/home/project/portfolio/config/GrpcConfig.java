package com.home.project.portfolio.config;

import com.home.project.portfolio.config.properties.TinkoffProperties;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.channel.ChannelOption;
import io.grpc.stub.MetadataUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.tinkoff.piapi.core.InstrumentsService;
import ru.tinkoff.piapi.core.InvestApi;
import ru.tinkoff.piapi.core.MarketDataService;
import ru.tinkoff.piapi.core.OperationsService;
import ru.tinkoff.piapi.core.UsersService;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static ru.tinkoff.piapi.core.InvestApi.addAppNameHeader;
import static ru.tinkoff.piapi.core.InvestApi.addAuthHeader;

/**
 * @author rlagay
 */
@Configuration
@RequiredArgsConstructor
public class GrpcConfig {

    private final TinkoffProperties tinkoffProperties;
    private ManagedChannel channel;

    @Value("${spring.application.name}")
    private String appName;


    @PostConstruct
    public void init() {
        channel = getChannel();
    }

    @Bean
    public UsersService usersService() {
        return InvestApi.create(channel).getUserService();
    }

    @Bean
    public OperationsService grpcOperationsService() {
        return InvestApi.create(channel).getOperationsService();
    }

    @Bean
    public InstrumentsService instrumentsService() {
        return InvestApi.create(channel).getInstrumentsService();
    }

    @Bean
    public MarketDataService marketDataService() {
        return InvestApi.create(channel).getMarketDataService();
    }

    @PreDestroy
    public void close() throws InterruptedException {
        try {
            channel.shutdown();
        } finally {
            if (!channel.awaitTermination(3, TimeUnit.SECONDS)) {
                channel.shutdownNow();
            }
        }
    }

    private ManagedChannel getChannel() {
        var headers = new Metadata();
        addAuthHeader(headers, tinkoffProperties.token());
        addAppNameHeader(headers, appName);
        var connectionTimeout = Duration.ofSeconds(10);
        return NettyChannelBuilder.forTarget(tinkoffProperties.url())
            .intercept(MetadataUtils.newAttachHeadersInterceptor(headers))
            .withOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) connectionTimeout.toMillis())
            .useTransportSecurity()
            .keepAliveWithoutCalls(true)
            .build();
    }
}
