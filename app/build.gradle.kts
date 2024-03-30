plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.letschat"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.letschat"
        minSdk = 27
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    dataBinding {
        enable = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    // Circle Image View
    implementation("de.hdodenhof:circleimageview:3.1.0")
    // Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")
    // Firebase Authentication
    implementation("com.google.firebase:firebase-auth:22.3.1")
    // BoM for Firebase platform
    implementation(platform("com.google.firebase:firebase-bom:32.7.4"))
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-firestore:24.10.3")
    implementation("com.google.firebase:firebase-storage:20.3.0")
    // FirebaseUI for Cloud Firestore
    implementation("com.firebaseui:firebase-ui-firestore:8.0.2")
    // Firebase Cloud Messaging
    implementation("com.google.firebase:firebase-messaging:23.4.1")
    // Zoomage View
    implementation("com.jsibbold:zoomage:1.3.1")
    // Pin View
    implementation("io.github.chaosleung:pinview:1.4.4")
    // Country Code Picker
    implementation("com.hbb20:ccp:2.5.0")
    // Image Picker
    implementation("com.github.dhaval2404:imagepicker:2.1")
    // define a BOM and its version
    implementation(platform("com.squareup.okhttp3:okhttp-bom:4.12.0"))
    // define any required OkHttp artifacts without version
    implementation("com.squareup.okhttp3:okhttp")
    // Android Reactions
    implementation("com.github.pgreze:android-reactions:1.6")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}