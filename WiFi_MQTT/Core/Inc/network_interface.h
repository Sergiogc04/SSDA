/*
 * network_interface.h
 *
 *  Created on: Apr 20, 2025
 *      Author: sirio
 */

#ifndef INC_NETWORK_INTERFACE_H_
#define INC_NETWORK_INTERFACE_H_

typedef struct Network {
    int (*mqttread)(struct Network*, unsigned char*, int, int);
    int (*mqttwrite)(struct Network*, unsigned char*, int, int);
} Network;

#endif /* INC_NETWORK_INTERFACE_H_ */
