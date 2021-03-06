/*
 * Copyright 2014 Click Travel Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.clicktravel.cheddar.server.application.status;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import com.clicktravel.cheddar.server.application.lifecycle.LifecycleStatus;
import com.clicktravel.cheddar.server.application.lifecycle.LifecycleStatusHolder;
import com.clicktravel.common.random.Randoms;

public class RestAdapterStatusHolderTest {

    private RestAdapterStatusHolder restAdapterStatusHolder;
    private LifecycleStatusHolder mockLifecycleStatusHolder;

    @Before
    public void setUp() {
        mockLifecycleStatusHolder = mock(LifecycleStatusHolder.class);
        restAdapterStatusHolder = new RestAdapterStatusHolder(mockLifecycleStatusHolder);
    }

    @Test
    public void shouldReturn0_onNoRequestsProcessed() {
        // When
        final int numRequests = restAdapterStatusHolder.restRequestsInProgress();

        // Then
        assertEquals(0, numRequests);
    }

    @Test
    public void shouldReturnCorrectNumber_afterRequestProcessingStarted() {
        // Given
        final int numRequests = Randoms.randomInt(10);
        startAndCompleteRequests(numRequests, 0);

        // When
        final int actualNumRequests = restAdapterStatusHolder.restRequestsInProgress();

        // Then
        assertEquals(numRequests, actualNumRequests);
    }

    @Test
    public void shouldReturn0_afterAllRequestsCompleted() {
        // Given
        final int numRequests = Randoms.randomInt(10);
        startAndCompleteRequests(numRequests, numRequests);

        // When
        final int actualNumRequests = restAdapterStatusHolder.restRequestsInProgress();

        // Then
        assertEquals(0, actualNumRequests);
    }

    @Test
    public void shouldReturnNumRequests_afterSomeRequestsCompleted() {
        // Given
        final int numRequests = Randoms.randomInt(10);
        final int numRequestsCompleted = Randoms.randomInt(numRequests);
        startAndCompleteRequests(numRequests, numRequestsCompleted);

        // When
        final int actualNumRequests = restAdapterStatusHolder.restRequestsInProgress();

        // Then
        assertEquals(numRequests - numRequestsCompleted, actualNumRequests);
    }

    @Test
    public void shouldReturnCorrectIsAcceptingRequests() {
        for (final LifecycleStatus lifecycleStatus : LifecycleStatus.values()) {
            shouldReturnCorrecIsAcceptingRequests_forLifecycleStatus(lifecycleStatus);
        }
    }

    private void shouldReturnCorrecIsAcceptingRequests_forLifecycleStatus(final LifecycleStatus lifecycleStatus) {
        // Given
        when(mockLifecycleStatusHolder.getLifecycleStatus()).thenReturn(lifecycleStatus);
        final boolean expectedIsAcceptingRequests = lifecycleStatus.equals(LifecycleStatus.PAUSED)
                || lifecycleStatus.equals(LifecycleStatus.RUNNING)
                || lifecycleStatus.equals(LifecycleStatus.HALTING_LOW_PRIORITY_EVENTS);

        // When
        final boolean actualIsAcceptingRequests = restAdapterStatusHolder.isAcceptingRequests();

        // Then
        assertEquals(expectedIsAcceptingRequests, actualIsAcceptingRequests);
    }

    private void startAndCompleteRequests(final int numRequests, final int numRequestsCompleted) {
        for (int n = 0; n < numRequests; n++) {
            restAdapterStatusHolder.requestProcessingStarted();
        }
        for (int n = 0; n < numRequestsCompleted; n++) {
            restAdapterStatusHolder.requestProcessingFinished();
        }
    }
}
