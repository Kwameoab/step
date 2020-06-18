// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.*;

public final class FindMeetingQuery {

  public Collection<TimeRange> query(
    Collection<Event> events,
    MeetingRequest request
  ) {
    Collection<TimeRange> firstAttempt = getQuery(events, request, true);
    if (firstAttempt.size() != 0) {
      return firstAttempt;
    } else {
      return getQuery(events, request, false);
    }
  }

  private Collection<TimeRange> getQuery(
    Collection<Event> events,
    MeetingRequest request,
    boolean optionalFlag
  ) {
    // if no one is attending then meeting can happening whenever
    if (request.getAttendees().size() == 0) {
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }

    // if meeting is long that a day it is impossible to occur
    if (request.getDuration() > TimeRange.WHOLE_DAY.duration()) {
      return Arrays.asList();
    }

    Collection<TimeRange> blockedTimes = new ArrayList<TimeRange>();
    Collection<TimeRange> returnTimeRange = new ArrayList<TimeRange>();
    TimeRange meetingTime = TimeRange.fromStartEnd(0, 0, false);

    blockedTimes = getBlockedTimes(events, request, optionalFlag);

    // if there are no other events at this point the meeting can happen whenever in the day
    if (blockedTimes.size() == 0) {
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }

    int startTime = 0;
    int endTime = 0;

    // The first time slot that should always be tested is from [0, the start time of the first event)
    endTime = blockedTimes.iterator().next().start();
    boolean isInbetween = getIsInbetween(blockedTimes, startTime);
    if (!isInbetween && endTime - startTime >= request.getDuration()) {
      meetingTime = TimeRange.fromStartEnd(startTime, endTime, false);
      returnTimeRange.add(meetingTime);
    }

    for (TimeRange time : blockedTimes) {
      startTime = time.end();

      endTime = getEndTime(blockedTimes, startTime);

      if (endTime == 0) {
        endTime = 24 * 60;
      }

      isInbetween = getIsInbetween(blockedTimes, startTime);

      if (!isInbetween && endTime - startTime >= request.getDuration()) {
        meetingTime = TimeRange.fromStartEnd(startTime, endTime, false);
        boolean haveTimeRange = getHaveTimeRange(meetingTime, returnTimeRange);
        if (!haveTimeRange) {
          returnTimeRange.add(meetingTime);
        }
      }
    }
    return returnTimeRange;
  }

  private Collection<TimeRange> getBlockedTimes(
    Collection<Event> events,
    MeetingRequest request,
    boolean optionalFlag
  ) {
    Collection<TimeRange> blockedTimes = new ArrayList<TimeRange>();
    for (Event event : events) {
      for (String eventAttendee : event.getAttendees()) {
        if (
          request.getAttendees().contains(eventAttendee) ||
          (
            optionalFlag &&
            request.getOptionalAttendees().contains(eventAttendee)
          )
        ) {
          TimeRange block = event.getWhen();
          blockedTimes.add(block);
        }
      }
    }
    return blockedTimes;
  }

  private Integer getEndTime(
    Collection<TimeRange> blockedTimes,
    int startTime
  ) {
    int possibleEndTime = 0;
    for (TimeRange start : blockedTimes) {
      possibleEndTime = start.start();
      if (possibleEndTime > startTime) {
        return possibleEndTime;
      }
    }
    return 0;
  }

  private boolean getIsInbetween(
    Collection<TimeRange> blockedTimes,
    int startTime
  ) {
    for (TimeRange oldStart : blockedTimes) {
      if (startTime < oldStart.end() && startTime >= oldStart.start()) {
        return true;
      }
    }
    return false;
  }

  private boolean getHaveTimeRange(
    TimeRange meetingTime,
    Collection<TimeRange> returnTimeRange
  ) {
    for (TimeRange alreadyHave : returnTimeRange) {
      if (meetingTime.contains(alreadyHave)) {
        return true;
      }
    }
    return false;
  }
}
