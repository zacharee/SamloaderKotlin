Pod::Spec.new do |spec|
    spec.name                     = 'composeentry'
    spec.version                  = '1.15.1'
    spec.homepage                 = 'https://zwander.dev'
    spec.source                   = { :http=> ''}
    spec.authors                  = ''
    spec.license                  = ''
    spec.summary                  = 'IDK'
    spec.vendored_frameworks      = 'build/cocoapods/framework/composeentry.framework'
    spec.libraries                = 'c++'
    spec.osx.deployment_target = '12.0'
                
                
    if !Dir.exist?('build/cocoapods/framework/composeentry.framework') || Dir.empty?('build/cocoapods/framework/composeentry.framework')
        raise "

        Kotlin framework 'composeentry' doesn't exist yet, so a proper Xcode project can't be generated.
        'pod install' should be executed after running ':generateDummyFramework' Gradle task:

            ./gradlew :composeentry:generateDummyFramework

        Alternatively, proper pod installation is performed during Gradle sync in the IDE (if Podfile location is set)"
    end
                
    spec.pod_target_xcconfig = {
        'KOTLIN_PROJECT_PATH' => ':composeentry',
        'PRODUCT_MODULE_NAME' => 'composeentry',
    }
                
    spec.script_phases = [
        {
            :name => 'Build composeentry',
            :execution_position => :before_compile,
            :shell_path => '/bin/sh',
            :script => <<-SCRIPT
                if [ "YES" = "$OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED" ]; then
                  echo "Skipping Gradle build task invocation due to OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED environment variable set to \"YES\""
                  exit 0
                fi
                set -ev
                REPO_ROOT="$PODS_TARGET_SRCROOT"
                "$REPO_ROOT/../gradlew" -p "$REPO_ROOT" $KOTLIN_PROJECT_PATH:syncFramework \
                    -Pkotlin.native.cocoapods.platform=$PLATFORM_NAME \
                    -Pkotlin.native.cocoapods.archs="$ARCHS" \
                    -Pkotlin.native.cocoapods.configuration="$CONFIGURATION"
            SCRIPT
        }
    ]
                
end