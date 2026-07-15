# РИД.МИ

## Архитектура

Проект разделён на feature-модули для экранов, финансовый
контракт `:finance:api`, реализации финансового контракта в `:finance:impl` и общие UI- и
design-system модули. 
Presentation-слой построен на MVI, а зависимости собираются вручную в `:app`.

Полное описание модулей, правил зависимостей итд находится в [arch.md](arch.md).

[GitHub Project](https://github.com/users/zagir005/projects/3) - тут веду учет по задачкам.

## Стек

- Kotlin
- Jetpack Compose
- Decompose
- Coroutines и Flow
- Ручной DI
