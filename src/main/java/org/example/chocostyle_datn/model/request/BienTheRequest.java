package org.example.chocostyle_datn.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class BienTheRequest {
    private Integer mauSacId;
    private List<KichCoRequest> sizeList;
    private List<String> hinhAnhUrls;

}
