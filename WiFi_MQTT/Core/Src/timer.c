/*
 * timer.c
 *
 *  Created on: Apr 20, 2025
 *      Author: sirio
 */

#include "timer.h"
#include "stm32l4xx_hal.h"
#include <stdint.h>

void TimerInit(Timer* timer) {
    timer->end_time = 0;
}

char TimerIsExpired(Timer* timer) {
    return (HAL_GetTick() >= timer->end_time);
}

void TimerCountdownMS(Timer* timer, unsigned int timeout) {
    timer->end_time = HAL_GetTick() + timeout;
}

void TimerCountdown(Timer* timer, unsigned int timeout) {
    timer->end_time = HAL_GetTick() + (timeout * 1000);  // Convierte el tiempo a milisegundos
}

int TimerLeftMS(Timer* timer) {
    int32_t left = (int32_t)(timer->end_time - HAL_GetTick());
    return (left < 0) ? 0 : left;
}

