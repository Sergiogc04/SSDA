################################################################################
# Automatically-generated file. Do not edit!
# Toolchain: GNU Tools for STM32 (13.3.rel1)
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
../Core/Src/MQTTClient.c \
../Core/Src/MQTTConnectClient.c \
../Core/Src/MQTTConnectServer.c \
../Core/Src/MQTTDeserializePublish.c \
../Core/Src/MQTTFormat.c \
../Core/Src/MQTTPacket.c \
../Core/Src/MQTTSerializePublish.c \
../Core/Src/MQTTSubscribeClient.c \
../Core/Src/MQTTSubscribeServer.c \
../Core/Src/MQTTUnsubscribeClient.c \
../Core/Src/MQTTUnsubscribeServer.c \
../Core/Src/es_wifi.c \
../Core/Src/es_wifi_io.c \
../Core/Src/hd44780.c \
../Core/Src/main.c \
../Core/Src/stm32l4xx_hal_msp.c \
../Core/Src/stm32l4xx_it.c \
../Core/Src/syscalls.c \
../Core/Src/sysmem.c \
../Core/Src/system_stm32l4xx.c \
../Core/Src/timer.c \
../Core/Src/wifi.c 

OBJS += \
./Core/Src/MQTTClient.o \
./Core/Src/MQTTConnectClient.o \
./Core/Src/MQTTConnectServer.o \
./Core/Src/MQTTDeserializePublish.o \
./Core/Src/MQTTFormat.o \
./Core/Src/MQTTPacket.o \
./Core/Src/MQTTSerializePublish.o \
./Core/Src/MQTTSubscribeClient.o \
./Core/Src/MQTTSubscribeServer.o \
./Core/Src/MQTTUnsubscribeClient.o \
./Core/Src/MQTTUnsubscribeServer.o \
./Core/Src/es_wifi.o \
./Core/Src/es_wifi_io.o \
./Core/Src/hd44780.o \
./Core/Src/main.o \
./Core/Src/stm32l4xx_hal_msp.o \
./Core/Src/stm32l4xx_it.o \
./Core/Src/syscalls.o \
./Core/Src/sysmem.o \
./Core/Src/system_stm32l4xx.o \
./Core/Src/timer.o \
./Core/Src/wifi.o 

C_DEPS += \
./Core/Src/MQTTClient.d \
./Core/Src/MQTTConnectClient.d \
./Core/Src/MQTTConnectServer.d \
./Core/Src/MQTTDeserializePublish.d \
./Core/Src/MQTTFormat.d \
./Core/Src/MQTTPacket.d \
./Core/Src/MQTTSerializePublish.d \
./Core/Src/MQTTSubscribeClient.d \
./Core/Src/MQTTSubscribeServer.d \
./Core/Src/MQTTUnsubscribeClient.d \
./Core/Src/MQTTUnsubscribeServer.d \
./Core/Src/es_wifi.d \
./Core/Src/es_wifi_io.d \
./Core/Src/hd44780.d \
./Core/Src/main.d \
./Core/Src/stm32l4xx_hal_msp.d \
./Core/Src/stm32l4xx_it.d \
./Core/Src/syscalls.d \
./Core/Src/sysmem.d \
./Core/Src/system_stm32l4xx.d \
./Core/Src/timer.d \
./Core/Src/wifi.d 


# Each subdirectory must supply rules for building sources it contributes
Core/Src/%.o Core/Src/%.su Core/Src/%.cyclo: ../Core/Src/%.c Core/Src/subdir.mk
	arm-none-eabi-gcc "$<" -mcpu=cortex-m4 -std=gnu11 -g3 -DDEBUG -DUSE_HAL_DRIVER -DSTM32L475xx -c -I../Core/Inc -I../Drivers/STM32L4xx_HAL_Driver/Inc -I../Drivers/STM32L4xx_HAL_Driver/Inc/Legacy -I../Drivers/CMSIS/Device/ST/STM32L4xx/Include -I../Drivers/CMSIS/Include -O0 -ffunction-sections -fdata-sections -Wall -fstack-usage -fcyclomatic-complexity -MMD -MP -MF"$(@:%.o=%.d)" -MT"$@" --specs=nano.specs -mfpu=fpv4-sp-d16 -mfloat-abi=hard -mthumb -o "$@"

clean: clean-Core-2f-Src

clean-Core-2f-Src:
	-$(RM) ./Core/Src/MQTTClient.cyclo ./Core/Src/MQTTClient.d ./Core/Src/MQTTClient.o ./Core/Src/MQTTClient.su ./Core/Src/MQTTConnectClient.cyclo ./Core/Src/MQTTConnectClient.d ./Core/Src/MQTTConnectClient.o ./Core/Src/MQTTConnectClient.su ./Core/Src/MQTTConnectServer.cyclo ./Core/Src/MQTTConnectServer.d ./Core/Src/MQTTConnectServer.o ./Core/Src/MQTTConnectServer.su ./Core/Src/MQTTDeserializePublish.cyclo ./Core/Src/MQTTDeserializePublish.d ./Core/Src/MQTTDeserializePublish.o ./Core/Src/MQTTDeserializePublish.su ./Core/Src/MQTTFormat.cyclo ./Core/Src/MQTTFormat.d ./Core/Src/MQTTFormat.o ./Core/Src/MQTTFormat.su ./Core/Src/MQTTPacket.cyclo ./Core/Src/MQTTPacket.d ./Core/Src/MQTTPacket.o ./Core/Src/MQTTPacket.su ./Core/Src/MQTTSerializePublish.cyclo ./Core/Src/MQTTSerializePublish.d ./Core/Src/MQTTSerializePublish.o ./Core/Src/MQTTSerializePublish.su ./Core/Src/MQTTSubscribeClient.cyclo ./Core/Src/MQTTSubscribeClient.d ./Core/Src/MQTTSubscribeClient.o ./Core/Src/MQTTSubscribeClient.su ./Core/Src/MQTTSubscribeServer.cyclo ./Core/Src/MQTTSubscribeServer.d ./Core/Src/MQTTSubscribeServer.o ./Core/Src/MQTTSubscribeServer.su ./Core/Src/MQTTUnsubscribeClient.cyclo ./Core/Src/MQTTUnsubscribeClient.d ./Core/Src/MQTTUnsubscribeClient.o ./Core/Src/MQTTUnsubscribeClient.su ./Core/Src/MQTTUnsubscribeServer.cyclo ./Core/Src/MQTTUnsubscribeServer.d ./Core/Src/MQTTUnsubscribeServer.o ./Core/Src/MQTTUnsubscribeServer.su ./Core/Src/es_wifi.cyclo ./Core/Src/es_wifi.d ./Core/Src/es_wifi.o ./Core/Src/es_wifi.su ./Core/Src/es_wifi_io.cyclo ./Core/Src/es_wifi_io.d ./Core/Src/es_wifi_io.o ./Core/Src/es_wifi_io.su ./Core/Src/hd44780.cyclo ./Core/Src/hd44780.d ./Core/Src/hd44780.o ./Core/Src/hd44780.su ./Core/Src/main.cyclo ./Core/Src/main.d ./Core/Src/main.o ./Core/Src/main.su ./Core/Src/stm32l4xx_hal_msp.cyclo ./Core/Src/stm32l4xx_hal_msp.d ./Core/Src/stm32l4xx_hal_msp.o ./Core/Src/stm32l4xx_hal_msp.su ./Core/Src/stm32l4xx_it.cyclo ./Core/Src/stm32l4xx_it.d ./Core/Src/stm32l4xx_it.o ./Core/Src/stm32l4xx_it.su ./Core/Src/syscalls.cyclo ./Core/Src/syscalls.d ./Core/Src/syscalls.o ./Core/Src/syscalls.su ./Core/Src/sysmem.cyclo ./Core/Src/sysmem.d ./Core/Src/sysmem.o ./Core/Src/sysmem.su ./Core/Src/system_stm32l4xx.cyclo ./Core/Src/system_stm32l4xx.d ./Core/Src/system_stm32l4xx.o ./Core/Src/system_stm32l4xx.su ./Core/Src/timer.cyclo ./Core/Src/timer.d ./Core/Src/timer.o ./Core/Src/timer.su ./Core/Src/wifi.cyclo ./Core/Src/wifi.d ./Core/Src/wifi.o ./Core/Src/wifi.su

.PHONY: clean-Core-2f-Src

