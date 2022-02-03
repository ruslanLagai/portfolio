package com.home.project.portfolio.model.response;

import com.home.project.portfolio.model.portfolio.Sector;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author rlagay
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SectorsDto {
    private List<Sector> sectors = new ArrayList<>();
}
