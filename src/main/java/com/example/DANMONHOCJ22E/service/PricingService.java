package com.example.DANMONHOCJ22E.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.MonthDay;
import java.time.temporal.ChronoUnit;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.example.DANMONHOCJ22E.model.RentalMode;
import com.example.DANMONHOCJ22E.model.Room;

@Service
public class PricingService {

    private static final BigDecimal HOLIDAY_SURCHARGE_FACTOR = new BigDecimal("1.80");

    private static final Set<MonthDay> HOLIDAYS = Set.of(
            MonthDay.of(1, 1),
            MonthDay.of(4, 30),
            MonthDay.of(5, 1),
            MonthDay.of(9, 2)
    );

    public BigDecimal calculateEstimatedPrice(Room room, RentalMode rentalMode, LocalDateTime checkInAt, LocalDateTime checkOutAt) {
        if (rentalMode == RentalMode.HOURLY) {
            long hours = ChronoUnit.HOURS.between(checkInAt, checkOutAt);
            if (ChronoUnit.MINUTES.between(checkInAt, checkOutAt) % 60 != 0) {
                hours += 1;
            }
            long billableHours = Math.max(hours, 1);
            return room.getHourlyRate().multiply(BigDecimal.valueOf(billableHours)).setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal total = BigDecimal.ZERO;
        LocalDate cursor = checkInAt.toLocalDate();
        LocalDate endDate = checkOutAt.toLocalDate();

        while (cursor.isBefore(endDate)) {
            BigDecimal dayPrice = room.getDailyRate();
            if (isHoliday(cursor)) {
                dayPrice = dayPrice.multiply(HOLIDAY_SURCHARGE_FACTOR);
            }
            total = total.add(dayPrice);
            cursor = cursor.plusDays(1);
        }

        return total.setScale(2, RoundingMode.HALF_UP);
    }

    public boolean isHoliday(LocalDate date) {
        return HOLIDAYS.contains(MonthDay.from(date));
    }
}
