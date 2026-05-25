package com.bbangle.bbangle.vat.seller.excel.service;

import com.bbangle.bbangle.vat.seller.controller.dto.request.SellerVatExcelType;
import com.bbangle.bbangle.vat.seller.excel.writer.SellerVatExcelWriter;
import com.bbangle.bbangle.vat.seller.provider.SellerVatSettlementProvider;
import com.bbangle.bbangle.vat.seller.provider.dto.SellerVatSettlementRow;
import com.bbangle.bbangle.vat.seller.service.model.SellerVatCommand.SellerVatSearchCommand;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SellerVatExcelService {

    private final SellerVatSettlementProvider settlementProvider;
    private final SellerVatExcelWriter excelWriter;

    public void writeExcel(
        SellerVatSearchCommand command,
        SellerVatExcelType type,
        OutputStream outputStream
    ) throws IOException {
        List<SellerVatSettlementRow> rows = settlementProvider.findSettlements(
            command.sellerId(),
            command.startMonth(),
            command.endMonth()
        );

        excelWriter.write(type, rows, outputStream);
    }
}
