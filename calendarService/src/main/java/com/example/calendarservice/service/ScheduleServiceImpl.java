package com.example.calendarservice.service;

import com.example.calendarservice.constant.RepeatType;
import com.example.calendarservice.dto.request.ScheduleRequestInsertDto;
import com.example.calendarservice.dto.request.ScheduleRequestUpdateDto;
import com.example.calendarservice.dto.request.SharedRequestInsertDto;
import com.example.calendarservice.dto.request.SharedRequestUpdateDto;
import com.example.calendarservice.dto.response.ScheduleResponseDayDto;
import com.example.calendarservice.dto.response.ScheduleResponseMonthDto;
import com.example.calendarservice.dto.response.ScheduleResponseYearDto;
import com.example.calendarservice.entity.Calendars;
import com.example.calendarservice.entity.Schedule;
import com.example.calendarservice.entity.ScheduleImage;
import com.example.calendarservice.repository.CalendarRepository;
import com.example.calendarservice.repository.ScheduleImageRepository;
import com.example.calendarservice.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.service.spi.ServiceException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.example.calendarservice.constant.Share.ALL;
import static com.example.calendarservice.constant.Share.CHOOSE;

@EnableAsync
@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final CalendarRepository calendarRepository;
    private final ScheduleImageRepository scheduleImageRepository;
    private final ImageService imageService;
    private final FileService fileService;
    private final SharedService sharedService;

    // 월달력 전체 일정 조회
    @Transactional
    @Override
    public List<ScheduleResponseMonthDto> findAllMonthSchedule(Long calendarIdx, int year, int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }
        if (year < 1 || year > 9999) {
            throw new IllegalArgumentException("Year must be a positive number and within the range of valid years");
        }
        if (!calendarRepository.existsById(calendarIdx)) {
            throw new IllegalArgumentException("캘린더가 존재하지 않습니다.");
        }

        try {
            LocalDate startOfMonth = LocalDate.of(year, month, 1);
            LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());

            List<Schedule> schedules = scheduleRepository.findByCalendarsIdxAndStartBetween(calendarIdx, startOfMonth.atStartOfDay(), endOfMonth.atTime(23, 59, 59));

            return schedules.stream()
                    .map(schedule -> ScheduleResponseMonthDto.builder()
                            .idx(schedule.getIdx())
                            .title(schedule.getTitle())
                            .start(schedule.getStart())
                            .end(schedule.getEnd())
                            .color(schedule.getColor())
                            .share(schedule.getShare())
                            .build())
                    .sorted(Comparator.comparing(ScheduleResponseMonthDto::getStart))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new ServiceException("Failed to find schedule in ScheduleService.findAllMonthSchedule", e);
        }
    }


    // 연달력 전체 일정 조회
    @Transactional
    @Override
    public List<ScheduleResponseYearDto> findAllYearSchedule(Long calendarIdx, int year) {
//        if (!calendarRepository.existsById(calendarIdx)) {
//            throw new CalendarsNotFoundException(CalendarsErrorCode.CALENDARS_NOT_FOUND);
//        }
//        if (year < 1 || year > 9999) {
//            throw new InvalidYear(CommonErrorCode.INVALID_YEAR);
//        }

        try {
            LocalDate startOfYear = LocalDate.of(year, 1, 1);
            LocalDate endOfYear = LocalDate.of(year, 12, 31);

            List<Schedule> schedules = scheduleRepository.findByCalendarsIdxAndStartBetween(calendarIdx, startOfYear.atStartOfDay(), endOfYear.atTime(23, 59, 59));

            return schedules.stream()
                    .map(schedule -> ScheduleResponseYearDto.builder()
                            .start(schedule.getStart())
                            .color(schedule.getColor())
                            .build())
                    .sorted(Comparator.comparing(ScheduleResponseYearDto::getStart))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new ServiceException("Failed to find schedule in ScheduleService.findAllYearSchedule", e);
        }
    }


    // 개별 날짜 일정 조회
    @Transactional
    @Override
    public List<ScheduleResponseDayDto> findScheduleByDay(Long calendarIdx, int year, int month, int day) {
//        if (!calendarRepository.existsById(calendarIdx)) {
//            throw new CalendarsNotFoundException(CalendarsErrorCode.CALENDARS_NOT_FOUND);
//        }
//        if (month < 1 || month > 12) {
//            throw new InvalidMonth(CommonErrorCode.INVALID_MONTH);
//        }
//        if (year < 1 || year > 9999) {
//            throw new InvalidYear(CommonErrorCode.INVALID_YEAR);
//        }
//        int lastDayOfMonth = YearMonth.of(year, month).lengthOfMonth();
//        if (day < 1 || day > lastDayOfMonth) {
//            throw new InvalidDay(CommonErrorCode.INVALID_DAY);
//        }

        try {
            LocalDate date = LocalDate.of(year, month, day);
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(23, 59, 59);

            List<Schedule> schedules = scheduleRepository.findSchedulesInDay(startOfDay, endOfDay, calendarIdx);

            return schedules.stream()
                    .map(schedule -> ScheduleResponseDayDto.builder()
                            .idx(schedule.getIdx())
                            .title(schedule.getTitle())
                            .content(schedule.getContent())
                            .start(schedule.getStart())
                            .end(schedule.getEnd())
                            .location(schedule.getLocation())
                            .color(schedule.getColor())
                            .repeatType(schedule.getRepeatType())
                            .repeatEndDate(schedule.getRepeatEndDate())
                            .share(schedule.getShare())
                            .images(schedule.getScheduleImages().stream()
                                    .map(ScheduleImage::getImgUrl)
                                    .collect(Collectors.toList()))
                            .build())
                    .sorted(Comparator.comparing(ScheduleResponseDayDto::getStart))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new ServiceException("Failed to find schedule in ScheduleService.findScheduleByDay", e);
        }
    }


    // 일정 1개 조회
    @Transactional
    @Override
    public ScheduleResponseDayDto findScheduleByOne(Long scheduleIdx) {
        // 유효성 검사
        Schedule schedule = scheduleRepository.findById(scheduleIdx)
                .orElseThrow(() -> new IllegalArgumentException("일정이 존재하지 않습니다."));

        try {
            List<String> imageUrls = schedule.getScheduleImages()
                    .stream()
                    .map(ScheduleImage::getImgUrl)
                    .collect(Collectors.toList());

            return ScheduleResponseDayDto.builder()
                    .title(schedule.getTitle())
                    .content(schedule.getContent())
                    .start(schedule.getStart())
                    .end(schedule.getEnd())
                    .location(schedule.getLocation())
                    .color(schedule.getColor())
                    .images(imageUrls)
                    .repeatType(schedule.getRepeatType())
                    .repeatEndDate(schedule.getRepeatEndDate())
                    .share(schedule.getShare())
                    .build();
        } catch (Exception e) {
            throw new ServiceException("Failed to find schedule in ScheduleService.findScheduleByOne", e);
        }
    }


    // 일정 입력
    @Transactional
    @Override
    public void saveSchedule(ScheduleRequestInsertDto scheduleRequestInsertDto, List<MultipartFile> imageFileList) {
        Calendars calendarIdx = calendarRepository.findById(scheduleRequestInsertDto.getCalendarIdx())
                .orElseThrow(() -> new IllegalArgumentException("캘린더가 존재하지 않습니다."));

        try {
            LocalDateTime currentStart = scheduleRequestInsertDto.getStart();
            LocalDateTime currentEnd = scheduleRequestInsertDto.getEnd();
            Long repeatGroupId = System.currentTimeMillis(); // 반복 그룹 ID 생성

            RepeatType effectiveRepeatType = scheduleRequestInsertDto.getRepeatType();
            LocalDate effectiveRepeatEndDate = scheduleRequestInsertDto.getRepeatEndDate();

            // 시작날짜, 종료날짜 차이가 하루 이상인지 확인
            long daysBetween = Duration.between(currentStart.toLocalDate().atStartOfDay(), currentEnd.toLocalDate().atStartOfDay()).toDays();

            // 차이가 하루 이상이고 반복 유형이 NONE인 경우 반복 유형을 DAILY로 설정하고 반복 종료 날짜를 end 날짜로 설정
//            if (daysBetween >= 1 && effectiveRepeatType == RepeatType.NONE) {
//                effectiveRepeatType = RepeatType.DAILY;
//                effectiveRepeatEndDate = currentEnd.toLocalDate();
//            }

            if (effectiveRepeatType == RepeatType.NONE) {
                effectiveRepeatEndDate = currentEnd.toLocalDate();
            }

            do {
                if (currentEnd.toLocalDate().isAfter(effectiveRepeatEndDate)) {
                    currentEnd = LocalDateTime.of(effectiveRepeatEndDate, currentEnd.toLocalTime());
                }

                Schedule createSchedule = Schedule.builder()
                        .title(scheduleRequestInsertDto.getTitle())
                        .content(scheduleRequestInsertDto.getContent())
                        .start(currentStart)
                        .end(currentEnd)
                        .location(scheduleRequestInsertDto.getLocation())
                        .color(scheduleRequestInsertDto.getColor())
                        .calendars(calendarIdx)
                        .repeatType(effectiveRepeatType)
                        .repeatEndDate(effectiveRepeatEndDate)
                        .repeatGroupId(repeatGroupId)
                        .share(scheduleRequestInsertDto.getShare())
                        .build();
                createSchedule = scheduleRepository.save(createSchedule);

                List<Long> friendIdxList = scheduleRequestInsertDto.getFriendIdxList();
                if (friendIdxList != null && !friendIdxList.isEmpty()){
                    for (Long friendIdx : friendIdxList){
                        SharedRequestInsertDto sharedRequestInsertDto = SharedRequestInsertDto.builder()
                                .scheduleIdx(createSchedule.getIdx())
                                .friendIdx(friendIdx)
                                .shareDateTime(LocalDateTime.now())
                                .build();
                        sharedService.saveShared(sharedRequestInsertDto);
                    }
                }else {
                    SharedRequestInsertDto sharedRequestInsertDto = SharedRequestInsertDto.builder()
                            .scheduleIdx(createSchedule.getIdx())
                            .shareDateTime(LocalDateTime.now())
                            .build();
                    sharedService.saveShared(sharedRequestInsertDto);
                }

                //이미지 저장 로직
                for (MultipartFile file : imageFileList) {
                    if (!file.isEmpty()) {
                        ScheduleImage scheduleImage = imageService.saveScheduleImage(file, createSchedule);
                        scheduleImage.setSchedule(createSchedule);
                        scheduleImageRepository.save(scheduleImage);
                    }
                }

                if (effectiveRepeatType == RepeatType.NONE) {
                    break; // 반복 없음 처리
                }

                switch (effectiveRepeatType) {
                    case DAILY:
                        currentStart = currentStart.plusDays(1);
                        currentEnd = currentEnd.plusDays(1);
                        break;
                    case WEEKLY:
                        currentStart = currentStart.plusWeeks(1);
                        currentEnd = currentEnd.plusWeeks(1);
                        break;
                    case MONTHLY:
                        currentStart = currentStart.plusMonths(1);
                        currentEnd = currentEnd.plusMonths(1);
                        break;
                    case YEARLY:
                        currentStart = currentStart.plusYears(1);
                        currentEnd = currentEnd.plusYears(1);
                        break;
                    case NONE:
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid repeat type");
                }
            } while (!currentStart.toLocalDate().isAfter(effectiveRepeatEndDate));

        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException("Failed to save schedule in ScheduleService.saveSchedule", e);
        }
    }

    // 일정 수정을 위해 필요한 삭제 메서드 (별도의 비동기 트랜잭션으로 삭제 처리)
    @Async
    @Transactional
    public CompletableFuture<Void> deleteExistingSchedules(Long repeatGroupId, LocalDateTime startDate) {
        scheduleRepository.deleteAfterDate(repeatGroupId, startDate);
        return CompletableFuture.completedFuture(null);
    }

    // 일정 수정
    @Transactional
    @Override
    public void updateSchedule(ScheduleRequestUpdateDto scheduleRequestUpdateDto, List<MultipartFile> imageFileList) {
        if (imageFileList == null) {
            imageFileList = Collections.emptyList();
        }

        Schedule updateSchedule = scheduleRepository.findById(scheduleRequestUpdateDto.getIdx())
                .orElseThrow(() -> new IllegalArgumentException("일정이 존재하지 않습니다."));

        try {
            boolean isRepeatTypeChanged = !updateSchedule.getRepeatType().equals(scheduleRequestUpdateDto.getRepeatType());
            LocalDate repeatEndDate = scheduleRequestUpdateDto.getRepeatEndDate();
            boolean isRepeatEndDateChanged = (updateSchedule.getRepeatEndDate() == null && repeatEndDate != null) ||
                    (updateSchedule.getRepeatEndDate() != null && !updateSchedule.getRepeatEndDate().equals(repeatEndDate));

            if (isRepeatTypeChanged || isRepeatEndDateChanged) {
                // 기존 반복 일정 삭제 (비동기로 처리)
                deleteExistingSchedules(updateSchedule.getRepeatGroupId(), updateSchedule.getStart())
                        .thenRun(() -> {
                            // 새로운 반복 일정 생성 로직
                            if (scheduleRequestUpdateDto.getRepeatType() == RepeatType.NONE) {
                                Long newRepeatGroupId = System.currentTimeMillis();

                                // 반복 없음으로 변경하는 경우: 현재 일정만 생성
                                Schedule newSchedule = Schedule.builder()
                                        .title(scheduleRequestUpdateDto.getTitle())
                                        .content(scheduleRequestUpdateDto.getContent())
                                        .start(scheduleRequestUpdateDto.getStart())
                                        .end(scheduleRequestUpdateDto.getEnd())
                                        .location(scheduleRequestUpdateDto.getLocation())
                                        .color(scheduleRequestUpdateDto.getColor())
                                        .calendars(updateSchedule.getCalendars())
                                        .share(scheduleRequestUpdateDto.getShare())
                                        .repeatType(RepeatType.NONE)
                                        .repeatEndDate(null)
                                        .repeatGroupId(newRepeatGroupId)
                                        .build();
                                if (scheduleRequestUpdateDto.getShare() != null){
                                    if (scheduleRequestUpdateDto.getShare() == ALL){
                                        SharedRequestInsertDto sharedRequestInsertDto = SharedRequestInsertDto.builder()
                                                .scheduleIdx(scheduleRequestUpdateDto.getIdx())
                                                .shareDateTime(LocalDateTime.now())
                                                .build();
                                        sharedService.updateToAllSchedule(sharedRequestInsertDto);
                                    } else if (scheduleRequestUpdateDto.getShare() == CHOOSE) {
                                        List<Long> friendIdxList = scheduleRequestUpdateDto.getFriendIdxList();
                                        if (friendIdxList != null && !friendIdxList.isEmpty()){
                                            for (Long friendIdx : friendIdxList){
                                                SharedRequestUpdateDto sharedRequestUpdateDto = SharedRequestUpdateDto.builder()
                                                        .scheduleIdx(scheduleRequestUpdateDto.getIdx())
                                                        .diaryIdx(null)
                                                        .friendIdx(friendIdx)
                                                        .shareDateTime(LocalDateTime.now())
                                                        .build();
                                                sharedService.updateShared(sharedRequestUpdateDto);
                                            }
                                        }
                                    } else {
                                        sharedService.deleteAllSharedByScheduleIdx(scheduleRequestUpdateDto.getIdx());
                                    }
                                    newSchedule.setShare(scheduleRequestUpdateDto.getShare());
                                }
                                scheduleRepository.save(newSchedule);
                            } else {
                                // 반복 타입 변경 또는 종료 날짜 변경: 새로운 반복 일정 생성
                                LocalDateTime currentStart = scheduleRequestUpdateDto.getStart();
                                LocalDateTime currentEnd = scheduleRequestUpdateDto.getEnd();
                                Long newRepeatGroupId = System.currentTimeMillis();
                                do {
                                    Schedule newSchedule = Schedule.builder()
                                            .title(scheduleRequestUpdateDto.getTitle())
                                            .content(scheduleRequestUpdateDto.getContent())
                                            .start(currentStart)
                                            .end(currentEnd)
                                            .location(scheduleRequestUpdateDto.getLocation())
                                            .color(scheduleRequestUpdateDto.getColor())
                                            .share(scheduleRequestUpdateDto.getShare())
                                            .calendars(updateSchedule.getCalendars())
                                            .repeatType(scheduleRequestUpdateDto.getRepeatType())
                                            .repeatEndDate(scheduleRequestUpdateDto.getRepeatEndDate())
                                            .repeatGroupId(newRepeatGroupId)
                                            .build();
                                    if (scheduleRequestUpdateDto.getShare() != null){
                                        if (scheduleRequestUpdateDto.getShare() == ALL){
                                            SharedRequestInsertDto sharedRequestInsertDto = SharedRequestInsertDto.builder()
                                                    .scheduleIdx(scheduleRequestUpdateDto.getIdx())
                                                    .shareDateTime(LocalDateTime.now())
                                                    .build();
                                            sharedService.updateToAllSchedule(sharedRequestInsertDto);
                                        } else if (scheduleRequestUpdateDto.getShare() == CHOOSE) {
                                            List<Long> friendIdxList = scheduleRequestUpdateDto.getFriendIdxList();
                                            if (friendIdxList != null && !friendIdxList.isEmpty()){
                                                for (Long friendIdx : friendIdxList){
                                                    SharedRequestUpdateDto sharedRequestUpdateDto = SharedRequestUpdateDto.builder()
                                                            .scheduleIdx(scheduleRequestUpdateDto.getIdx())
                                                            .diaryIdx(null)
                                                            .friendIdx(friendIdx)
                                                            .shareDateTime(LocalDateTime.now())
                                                            .build();
                                                    sharedService.updateShared(sharedRequestUpdateDto);
                                                }
                                            }
                                        } else {
                                            sharedService.deleteAllSharedByScheduleIdx(scheduleRequestUpdateDto.getIdx());
                                        }
                                        newSchedule.setShare(scheduleRequestUpdateDto.getShare());
                                    }
                                    scheduleRepository.save(newSchedule);

                                    // 반복 설정에 따른 날짜 계산
                                    switch (scheduleRequestUpdateDto.getRepeatType()) {
                                        case DAILY:
                                            currentStart = currentStart.plusDays(1);
                                            currentEnd = currentEnd.plusDays(1);
                                            break;
                                        case WEEKLY:
                                            currentStart = currentStart.plusDays(7);
                                            currentEnd = currentEnd.plusDays(7);
                                            break;
                                        case MONTHLY:
                                            currentStart = currentStart.plusMonths(1);
                                            currentEnd = currentEnd.plusMonths(1);
                                            break;
                                        case YEARLY:
                                            currentStart = currentStart.plusYears(1);
                                            currentEnd = currentEnd.plusYears(1);
                                            break;
                                        default:
                                            throw new IllegalArgumentException("Invalid repeat type");
                                    }
                                } while (!currentStart.toLocalDate().isAfter(scheduleRequestUpdateDto.getRepeatEndDate()));
                            }
                        });
            } else {
                // 반복 설정이 변경되지 않은 경우: 현재 일정만 수정
                updateSchedule.setTitle(scheduleRequestUpdateDto.getTitle());
                updateSchedule.setContent(scheduleRequestUpdateDto.getContent());
                updateSchedule.setStart(scheduleRequestUpdateDto.getStart());
                updateSchedule.setEnd(scheduleRequestUpdateDto.getEnd());
                updateSchedule.setLocation(scheduleRequestUpdateDto.getLocation());
                updateSchedule.setColor(scheduleRequestUpdateDto.getColor());
                updateSchedule.setShare(scheduleRequestUpdateDto.getShare());
                if (scheduleRequestUpdateDto.getShare() != null){
                    if (scheduleRequestUpdateDto.getShare() == ALL){
                        SharedRequestInsertDto sharedRequestInsertDto = SharedRequestInsertDto.builder()
                                .scheduleIdx(scheduleRequestUpdateDto.getIdx())
                                .shareDateTime(LocalDateTime.now())
                                .build();
                        sharedService.updateToAllSchedule(sharedRequestInsertDto);
                    } else if (scheduleRequestUpdateDto.getShare() == CHOOSE) {
                        List<Long> friendIdxList = scheduleRequestUpdateDto.getFriendIdxList();
                        if (friendIdxList != null && !friendIdxList.isEmpty()){
                            for (Long friendIdx : friendIdxList){
                                SharedRequestUpdateDto sharedRequestUpdateDto = SharedRequestUpdateDto.builder()
                                        .scheduleIdx(scheduleRequestUpdateDto.getIdx())
                                        .diaryIdx(null)
                                        .friendIdx(friendIdx)
                                        .shareDateTime(LocalDateTime.now())
                                        .build();
                                sharedService.updateShared(sharedRequestUpdateDto);
                            }
                        }
                    } else {
                        sharedService.deleteAllSharedByScheduleIdx(scheduleRequestUpdateDto.getIdx());
                    }
                    updateSchedule.setShare(scheduleRequestUpdateDto.getShare());
                }
            }

            // 이미지 삭제
            List<String> deleteImageList = scheduleRequestUpdateDto.getDeletedImageList();
            if (deleteImageList != null && !deleteImageList.isEmpty()) {
                for (String imageUrl : deleteImageList) {
                    fileService.deleteFile(imageUrl);
                    scheduleImageRepository.deleteByImgUrl(imageUrl);
                }
            }

            // 이미지 업데이트 로직
            for (MultipartFile file : imageFileList) {
                if (!file.isEmpty()) {
                    ScheduleImage scheduleImage = imageService.saveScheduleImage(file, updateSchedule);
                    scheduleImage.setSchedule(updateSchedule);
                    scheduleImageRepository.save(scheduleImage);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //드래그 앤 드랍 관련
    @Transactional
    @Override
    public void updateScheduleDate(Long scheduleIdx, ScheduleRequestUpdateDto scheduleRequestUpdateDto) {
        // 일정 조회
        Schedule schedule = scheduleRepository.findById(scheduleIdx)
                .orElseThrow(() -> new IllegalArgumentException("fail"));

        try {
            // 시작 날짜 및 종료 날짜 업데이트
            schedule.setStart(scheduleRequestUpdateDto.getStart());
            schedule.setEnd(scheduleRequestUpdateDto.getEnd());
            scheduleRepository.save(schedule); // 변경 사항 저장
        } catch (Exception e) {
            throw new ServiceException("Failed to update schedule date in ScheduleService.updateScheduleDate", e);
        }
    }


    // 일정 삭제
    @Transactional
    @Override
    public void deleteSchedule(Long scheduleId, boolean deleteAllRepeats, boolean deleteOnlyThis, boolean deleteAfter) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("일정이 존재하지 않습니다."));

        try {
            if (deleteAllRepeats) {
                // 반복 일정 전체 삭제 로직
                List<Schedule> repeatSchedules = scheduleRepository.findByRepeatGroupId(schedule.getRepeatGroupId());
                scheduleRepository.deleteAll(repeatSchedules);
            } else if (deleteAfter) {
                // 현재 일정 포함 이후 일정 모두 삭제 로직
                scheduleRepository.deleteAfterDate(schedule.getRepeatGroupId(), schedule.getStart());
            } else if (deleteOnlyThis) {
                // 현재 일정만 삭제 로직
                scheduleRepository.delete(schedule);
            }
        } catch (Exception e) {
            throw new ServiceException("Failed to delete schedule in ScheduleService.deleteSchedule", e);
        }
    }
}
