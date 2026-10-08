package com.ramkrishna.sales.service;

import com.ramkrishna.sales.dto.ApiDtos.ListPageDto;
import com.ramkrishna.sales.dto.ApiDtos.PaginationDto;
import com.ramkrishna.sales.exception.Exceptions;
import java.util.Locale;
import java.util.function.Function;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

final class ListPagination {
	private static final int MAX_LIMIT = 50;

	private ListPagination() {
	}

	static PageRequest pageRequest(int page, int limit) {
		if (page < 1)
			throw new Exceptions.BadRequest("Page must be 1 or greater");
		if (limit < 1)
			throw new Exceptions.BadRequest("Limit must be 1 or greater");
		return PageRequest.of(page - 1, Math.min(limit, MAX_LIMIT),
				Sort.by(Sort.Order.asc("name"), Sort.Order.asc("id")));
	}

	static String normalizeSearch(String search) {
		if (search == null || search.isBlank())
			return null;
		return search.strip().toLowerCase(Locale.ROOT)
				.replace("\\", "\\\\")
				.replace("%", "\\%")
				.replace("_", "\\_");
	}

	static <S, T> ListPageDto<T> response(Page<S> result, Function<S, T> mapper) {
		return new ListPageDto<>(result.map(mapper).getContent(),
				new PaginationDto(result.getNumber() + 1, result.getSize(), result.getTotalElements(),
						result.getTotalPages()));
	}
}
