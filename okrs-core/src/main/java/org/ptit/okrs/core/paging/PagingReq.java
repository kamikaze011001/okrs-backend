package org.ptit.okrs.core.paging;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.apache.commons.collections4.MapUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import javax.validation.constraints.Min;
import java.util.*;
import java.util.stream.Collectors;

@Data
public class PagingReq {

    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int DEFAULT_PAGE_NUM = 1;

    @Min(1)
    private Integer pageSize = DEFAULT_PAGE_SIZE;

    @Min(1)
    private Integer pageNum = DEFAULT_PAGE_NUM;

    private String sorts;

    private LinkedHashMap<String, Sort.Direction> sortMap;

    @JsonIgnore
    public Pageable makePageable() {
        List<Sort.Order> orders = createOrders(getSortMap());
        // Pageable is 0 base, PageRequest is 1 base
        return PageRequest.of(getPageNum() - 1, getPageSize(), Sort.by(orders));
    }

    @JsonIgnore
    public void setSortDefault(LinkedHashMap<String, Sort.Direction> sortDefault) {
        setSortMap(Optional.ofNullable(getSortMap())
                .filter(MapUtils::isNotEmpty)
                .orElse(sortDefault));
    }

    private List<Sort.Order> createOrders(Map<String, Sort.Direction> sorts) {
        return Optional.ofNullable(sorts)
                .filter(MapUtils::isNotEmpty)
                .map(Map::entrySet)
                .map(entries -> entries.stream()
                        .map(e -> new Sort.Order(e.getValue(), e.getKey()))
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }
}

