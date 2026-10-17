# Shulker Insight

[English](#english) | [Русский](#русский)

## English

A client-side Fabric mod for Minecraft that helps you see what's inside shulker boxes.

### Features

- Contents preview in the item tooltip.
- Fill indicator in the inventory.
- Item badge in the inventory when a shulker box contains a single item type.
- Item icon on a placed shulker box after opening it, when it contains a single item type.

The client doesn't know the contents of a closed placed shulker box until it has been opened at least once in the current session, so the world icon appears only after the first opening.

### Settings

Open the settings with the `Left` key by default.

- Contents preview, fill indicator, inventory badge, world icon: on/off.
- World icon distance: 4–64 blocks, step 4.
- World icon scale: 25–100 %, step 5 %.

### Requirements

- JDK 25
- Minecraft 26.2 or 26.3
- Fabric API

### Development

The mod is written in Kotlin. With direnv, run `direnv allow` first.

```sh
./gradlew build
./gradlew test
./gradlew runClient
```

The build targets Minecraft 26.3 by default. To build for another version, override the Minecraft and Fabric API versions:

```sh
./gradlew build -Pminecraft_version=26.2 -Pfabric_version=0.152.1+26.2
```

## Русский

Клиентский мод Fabric для Minecraft, помогающий видеть содержимое шалкеров.

### Возможности

- Предпросмотр содержимого во всплывающей подсказке.
- Индикатор заполненности в инвентаре.
- Бейдж предмета в инвентаре, если шалкер содержит один тип предметов.
- Иконка предмета на установленном в мире шалкере после его открытия, если шалкер содержит один тип предметов.

Клиент не знает содержимое закрытого установленного шалкера, пока тот не будет открыт хотя бы раз в текущей сессии, поэтому иконка в мире появляется только после первого открытия.

### Настройки

Настройки открываются клавишей `Left` по умолчанию.

- Предпросмотр содержимого, индикатор заполненности, бейдж в инвентаре и иконка в мире: вкл./выкл.
- Дальность иконки в мире: 4–64 блока, шаг 4 блока.
- Масштаб иконки в мире: 25–100 %, шаг 5 %.

### Требования

- JDK 25
- Minecraft 26.2 или 26.3
- Fabric API

### Разработка

Мод написан на Kotlin. При использовании direnv сначала выполните `direnv allow`.

```sh
./gradlew build
./gradlew test
./gradlew runClient
```

По умолчанию сборка идёт под Minecraft 26.3. Для другой версии переопределите версии Minecraft и Fabric API:

```sh
./gradlew build -Pminecraft_version=26.2 -Pfabric_version=0.152.1+26.2
```
