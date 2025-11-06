package com.hhplus.ecommerce.domain.vo;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.regex.Pattern;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class Phone implements Serializable {

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^01[0-9]-?[0-9]{3,4}-?[0-9]{4}$");

    private String number;

    public Phone(String number) {
        validateNumber(number);
        this.number = number;
    }

    private void validateNumber(String number) {
        if (number == null || number.isBlank()) {
            throw new IllegalArgumentException("전화번호는 비어있을 수 없습니다.");
        }
        if (!PHONE_PATTERN.matcher(number).matches()) {
            throw new IllegalArgumentException("올바른 전화번호 형식이 아닙니다. (예: 010-1234-5678)");
        }
    }

    @Override
    public String toString() {
        return number;
    }
}
