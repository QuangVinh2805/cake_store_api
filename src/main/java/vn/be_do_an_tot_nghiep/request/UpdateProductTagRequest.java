package vn.be_do_an_tot_nghiep.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProductTagRequest {
    private String productHashId;
    private List<String> oldTags;
    private List<String> newTags;
}

