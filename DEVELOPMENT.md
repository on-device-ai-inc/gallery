# Development notes

## Build app locally

To successfully build and run the application through Android Studio, you need to configure it with your own HuggingFace Developer Application ([official doc](https://huggingface.co/docs/hub/oauth#creating-an-oauth-app)). This is required for the model download functionality to work correctly.

After you've created a developer application:

1. In [`ProjectConfig.kt`](https://github.com/on-device-ai-inc/on-device-ai/blob/main/Android/src/app/src/main/java/ai/ondevice/app/common/ProjectConfig.kt), replace the placeholders for `clientId` and `redirectUri` with the values from your HuggingFace developer application.

1. In [`app/build.gradle.kts`](https://github.com/on-device-ai-inc/on-device-ai/blob/main/Android/src/app/build.gradle.kts), modify the `manifestPlaceholders["appAuthRedirectScheme"]` value to match the redirect URL you configured in your HuggingFace developer application.
