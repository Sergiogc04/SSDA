/*
 * timer.h
 *
 *  Created on: Apr 20, 2025
 *      Author: sirio
 */

#ifndef INC_TIMER_H_
#define INC_TIMER_H_
#include <stdint.h>


typedef struct Timer
{
	 uint32_t  end_time;
} Timer;

void TimerInit(Timer*);
char TimerIsExpired(Timer*);
void TimerCountdownMS(Timer*, unsigned int);
void TimerCountdown(Timer*, unsigned int);
int TimerLeftMS(Timer*);


#endif /* INC_TIMER_H_ */
