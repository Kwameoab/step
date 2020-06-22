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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class FindMeetingQuery {

  /**
   * Gets collection of TimeRange of available times for meeting given events of
   * attendees, and the request. First checks if it is possible to have meeting with
   * all optional attendees, if not then check if possible with no optional
   *
   * @param events the events that the attendees have to visit
   * @param request the request that is made including the attendees visiting
   * @return Collection of TimeRanges that are available for attendees
   */
  public Collection<TimeRange> query(
    Collection<Event> events,
    MeetingRequest request
  ) {
    // firstAttempt is using the query to find a collection of timeranges including all optional attendees
    Collection<TimeRange> firstAttempt = getQuery(events, request, true);

    if (firstAttempt.size() != 0) {
      return firstAttempt;
    } else {
      // if it was not possible to get a collection of timeranges including all
      // optional attendees then try to get timerange without optional attendees
      return getQuery(events, request, false);
    }
  }

  /**
   * Gets collection of TimeRange of available times for meeting given events of
   * attendees, the request, and if optional attendees should be considered
   *
   * @param events the events that the attendees have to visit
   * @param request the request that is made including the attendees visiting
   * @param considerOptional whether or not optional attendees are considered
   * @return Collection of TimeRanges that are available for attendees
   */
  private Collection<TimeRange> getQuery(
    Collection<Event> events,
    MeetingRequest request,
    boolean considerOptional
  ) {
    // if no one is attending then meeting can happening whenever
    if (
      request.getAttendees().size() == 0 &&
      request.getOptionalAttendees().size() == 0
    ) {
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }

    // if the meeting is longer than a day it is impossible to occur
    if (request.getDuration() > TimeRange.WHOLE_DAY.duration()) {
      return Arrays.asList();
    }

    Collection<TimeRange> blockedTimes = new ArrayList<TimeRange>();
    Collection<TimeRange> returnTimeRange = new ArrayList<TimeRange>();
    TimeRange meetingTime = TimeRange.fromStartEnd(0, 0, false);

    blockedTimes = getBlockedTimes(events, request, considerOptional);

    // if there are no other events at this point the meeting can happen whenever in the day
    if (
      blockedTimes.size() == 0 && request.getOptionalAttendees().size() == 0
    ) {
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }

    int startTime = 0;
    int endTime = 0;

    // the first time slot that should always be tested is from [0, the start time of the first event)
    if (blockedTimes.iterator().hasNext()) {
      endTime = blockedTimes.iterator().next().start();
    }

    boolean isInbetween = getIsInbetween(blockedTimes, startTime);

    // if startTime and endTime are not inbetween any other blocks and longer
    // than request duration then it can be added to returnTimeRange
    if (!isInbetween && endTime - startTime >= request.getDuration()) {
      meetingTime = TimeRange.fromStartEnd(startTime, endTime, false);
      returnTimeRange.add(meetingTime);
    }

    // for each TimeRange in blockedTimes use the end time of the TimeRange to
    // be new possible start time, get an endTime from next possible TimeRange,
    // check if these new times are inbetween blocks, if they are not add it returnTimeRange
    for (TimeRange time : blockedTimes) {
      startTime = time.end();

      endTime = getEndTime(blockedTimes, startTime);

      isInbetween = getIsInbetween(blockedTimes, startTime);

      // if startTime and endTime are not inbetween any other blocks and longer
      // than request duration then it can be added to returnTimeRange
      if (!isInbetween && endTime - startTime >= request.getDuration()) {
        meetingTime = TimeRange.fromStartEnd(startTime, endTime, false);

        // there are cases where the same time range can be added, this avoid that
        boolean alreadyHaveTimeRange = getAlreadyHaveTimeRange(
          meetingTime,
          returnTimeRange
        );
        if (!alreadyHaveTimeRange) {
          returnTimeRange.add(meetingTime);
        }
      }
    }
    return returnTimeRange;
  }

  /**
   * Gets collection of TimeRange of blocked of times given events of attendees,
   * the request, and if optional attendees should be considered
   *
   * @param events the events that the attendees have to visit
   * @param request the request that is made including the attendees visiting
   * @param considerOptional whether or not optional attendees are considered
   * @return Collection of TimeRanges that are blocked for attendees
   */
  private Collection<TimeRange> getBlockedTimes(
    Collection<Event> events,
    MeetingRequest request,
    boolean considerOptional
  ) {
    List<TimeRange> blockedTimes = new ArrayList<TimeRange>();
    for (Event event : events) {
      for (String eventAttendee : event.getAttendees()) {
        boolean isRequestAttendee = request
          .getAttendees()
          .contains(eventAttendee);

        // if we need to consider optional attendees then we need to also block off their events
        boolean isOptionalAttendee =
          considerOptional &&
          request.getOptionalAttendees().contains(eventAttendee);

        if (isRequestAttendee || isOptionalAttendee) {
          TimeRange block = event.getWhen();
          blockedTimes.add(block);
        }
      }
    }
    // all other functions require that blockedTimes is sorted low to high by start time
    Collections.sort(blockedTimes, TimeRange.ORDER_BY_START);
    return blockedTimes;
  }

  /**
   * Given a start time of a possible returnTimeRange finds the next closest end
   * time using blockedTimes
   *
   * @param blockedTimes the time slots that can not be used in returnTimeRange
   * @param startTime the start time of a slot in returnTimeRange
   * @return endTime given startTime, returns 24 * 60(the end of the day) if no
   *         endTIme could be found through blockedTImes
   */
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
    // can only get here if no possibleEndTime was found
    return 24 * 60;
  }

  /**
   * Given a start time of a possible returnTimeRange returns whether or not it
   * inbetween a blocked time range using blockedTimes
   *
   * @param blockedTimes the time slots that can not be used in returnTimeRange
   * @param startTime the start time of a slot in returnTimeRange
   * @return true if start time is inbetween a blocked time false if not
   */
  private boolean getIsInbetween(
    Collection<TimeRange> blockedTimes,
    int startTime
  ) {
    for (TimeRange block : blockedTimes) {
      if (startTime < block.end() && startTime >= block.start()) {
        return true;
      }
    }
    return false;
  }

  /**
   * Given a start time of a possible returnTimeRange returns whether or not it
   * inbetween a blocked time range using blockedTimes
   *
   * @param meetingTime a possible TimeRange that could be added to returnTimeRange
   * @param returnTimeRange the return TimeRange of all possible meeting times
   * @return true if any time of meetingTime is already included in returnTimeRange
   */
  private boolean getAlreadyHaveTimeRange(
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
