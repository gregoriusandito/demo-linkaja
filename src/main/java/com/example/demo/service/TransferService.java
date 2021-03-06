package com.example.demo.service;

import com.example.demo.domain.SaldoResponse;
import com.example.demo.domain.TransferRequest;
import com.example.demo.exceptions.GeneralException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TransferService {
    @Autowired
    AccountService accountService;

    public SaldoResponse transfer(Long fromAccountNumber, TransferRequest transferRequest){
        SaldoResponse updateFromAccountBalance = null;
        SaldoResponse updateDestinationBalance = null;
        SaldoResponse fromAccountDetail = null;
        SaldoResponse toAccountDetail;

        try {
            fromAccountDetail = accountService.getSaldo(fromAccountNumber);
            toAccountDetail = accountService.getSaldo(transferRequest.getToAccountNumber());
        } catch (GeneralException ex) {
            if (fromAccountDetail == null) {
                throw new GeneralException("SRCNOTFOUND", "Source Account not Found", "SAF".concat(generateRandomRef().toString()));
            } else {
                throw new GeneralException("DSTNOTFOUND", "Beneficiary Account not Found", "BAF".concat(generateRandomRef().toString()));
            }
        }
        log.info("fr {}", fromAccountDetail);
        log.info("to {}", toAccountDetail);

        if (fromAccountDetail.getBalance().compareTo(transferRequest.getAmount()) == -1 ) {
            throw new GeneralException("INSUFBAL", "Insufficient Balance", "IBA".concat(generateRandomRef().toString()));
        } else {
            updateDestinationBalance = SaldoResponse.builder()
                    .accountNumber(toAccountDetail.getAccountNumber())
                    .balance(toAccountDetail.getBalance().add(transferRequest.getAmount()))
                    .customerName(toAccountDetail.getCustomerName())
                    .build();

            updateFromAccountBalance = SaldoResponse.builder()
                    .accountNumber(fromAccountDetail.getAccountNumber())
                    .balance(fromAccountDetail.getBalance().subtract(transferRequest.getAmount()))
                    .customerName(fromAccountDetail.getCustomerName())
                    .build();


            accountService.updateSaldo(updateDestinationBalance, toAccountDetail.getCustomerNumber());
            accountService.updateSaldo(updateFromAccountBalance, fromAccountDetail.getCustomerNumber());
        }

        return updateDestinationBalance;
    }

    private Integer generateRandomRef() {
        int min = 1000;
        int max = 10000;
        int randomInt;
        return randomInt = (int)(Math.random() * (max - min + 1) + min);
    }

}
