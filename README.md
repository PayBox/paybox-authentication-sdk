# paybox-authentication-sdk
Authentication SDK for Android by PayBox.money

```diff
! Идентификация доступна для устройств с версией Android 5.0 и выше
! Идентификация доступна при полученном разрешении на использование камеры
```

Библиотека для идентификации пользователей

# **Установка:**

1. Добавьте репозитории Jitpack в ваш build.gradle или settings.gradle :
  * a) Для build.gradle на уровне проекта в конец репозиториев allprojects
```
allprojects {
    repositories {
        // ...
        maven { url "https://jitpack.io" }
    }
}
```
  - b) Для settings.gradle в конец репозиториев dependencyResolutionManagement
```
dependencyResolutionManagement {
    // ...
    repositories {
        // ...
        maven { url "https://jitpack.io" }
    }
}
```

2.Добавьте в ваш build.gradle:
```
dependencies {
	implementation 'com.github.PayBox:paybox-authentication-sdk:1.0.0'
}
```
---
# Настройка SDK
1. Для отслеживания прогресса загрузки и получения ответа от SDK нeобходимо имплементировать интерфейс EventListener в вашем Activity:
```
class MainActivity : AppCompatActivity(), EventListener {
  // ...

  override fun onLoadStarted() {
        //TODO: Show progress bar
    }

    override fun onLoadFinished() {
        //TODO: Hide progress bar
    }
    override fun onError(message: String) {
        //TODO: Resolve errors occurred during SDK's work
    }

    override fun onAuth(result: AuthResult) {
        //TODO: Your implementation using auth result
        when(result.status) {
            AuthResult.Status.NEW -> {
              //Требуется подтверждение идентификации с помощью OTP (Промежуточный)
            }
            AuthResult.Status.APPROVED -> {
              //Необходимо отправить лучшее изображение идентифицируемого (Промежуточный)
            }
            AuthResult.Status.PROCESS -> {
              //Запрашивать статус до финального статуса (Промежуточный)
            }
            AuthResult.Status.VERIFIED -> {
              //Запрашивать статус до финального статуса (Промежуточный)
            }
            AuthResult.Status.IDENTIFIED -> {
              //Успешный результат идентификации (Финальный)
            }
            AuthResult.Status.ERROR -> {
              //Неудачный результат идентификации (Финальный)
            }
        }
    }
    
    override fun onCheck(result: AuthResult) {
        //TODO: Your implementation using auth result
        when(result.status) {
            AuthResult.Status.NEW -> {
              //Требуется подтверждение идентификации с помощью OTP (Промежуточный)
            }
            AuthResult.Status.APPROVED -> {
              //Необходимо отправить лучшее изображение идентифицируемого (Промежуточный)
            }
            AuthResult.Status.PROCESS -> {
              //Запрашивать статус до финального статуса (Промежуточный)
            }
            AuthResult.Status.VERIFIED -> {
              //Запрашивать статус до финального статуса (Промежуточный)
            }
            AuthResult.Status.IDENTIFIED -> {
              //Успешный результат идентификации (Финальный)
            }
            AuthResult.Status.ERROR -> {
              //Неудачный результат идентификации (Финальный)
            }
        }
    }
}  
```

2. Добавьте AuthView в ваше activity:
 ```
 <money.paybox.authentication_sdk.ui.AuthView
        android:id="@+id/authView"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
 ```

3. Инициализация SDK:
```
val authView = findViewById<AuthView>(R.id.authView)
val authSDK = PayBoxAuth.Builder(this)
            .setAuthView(authView)
            .setSlug("your_slug_key")
            .setSecretKey("your_secret_key")
            .setToken("your_token")
            .setLanguage(Language.ru)
            .build()
```
---

# **Работа с SDK**

## *Идентификация:*
```diff
! Идентификация доступна для устройств с версией Android 5.0 и выше
! Идентификация доступна при полученном разрешении на использование камеры
```
Для идентификации вызвать метод с передачей номера телефона клиента
```
if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
  authSDK.auth("77771112233")
} else {
  //Request CAMERA permission to use authentication SDK
}
```
После вызова в authView откроется страница с формой для ввода персональных данных необходимых для идентификации, после чего происходит сам процесс идентификации<br>
Результат приходит в перегружаемый метод onAuth(result: AuthResult) имплементированного интерфеса EventListener


## *Проверка статуса последней попытки:*
```
authSDK.checkLast()
```
Результат приходит в перегружаемый метод onCheck(result: AuthResult) имплементированного интерфеса EventListener


## *Проверка статуса по id:*
```
authSDK.checkStatusById(123)
```
Результат приходит в перегружаемый метод onCheck(result: AuthResult) имплементированного интерфеса EventListener


## *Проверка статуса по номеру телефона:*
```
authSDK.checkStatusByPhone("77771112233")
```
Результат приходит в перегружаемый метод onCheck(result: AuthResult) имплементированного интерфеса EventListener
