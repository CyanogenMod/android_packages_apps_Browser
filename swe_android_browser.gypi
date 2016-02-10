{
  'variables' : {
    #This needs to be in sync with java package name, required to generate R.java
    'swe_browser_java_package': 'com.android.browser',
  },
  'targets' : [
    {
        'target_name': 'ambient_sdk-no-res',
        'type': 'none',
        'variables': {
            'jar_path': './lib/ambientsdk.jar',
        },
        'includes': ['../../build/java_prebuilt.gypi'],
    },
    {
        'target_name': 'ambient_sdk',
        'type': 'none',
        'variables': {
            'java_in_dir': './ambientsdk/sdk',
            'R_package': ['com.cyanogen.ambient'],
            'R_package_relpath': ['com/cyanogen/ambient'],
            'has_java_resources': 1,
            'run_findbugs': 0,
        },
        'dependencies': [
            'ambient_sdk-no-res'
        ],
        'includes': ['../../build/java.gypi'],
    },
    {
      'target_name': 'swe_android_browser_apk',
      'type': 'none',
      'dependencies': [
        'ambient_sdk',
        'swe_android_browser_apk_config',
        '<@(swe_dependencies)',
        #add new dependencies in swe_common.gypi
      ],
      'variables': {
        'apk_name': 'SWE_AndroidBrowser',
        'native_lib_version_name': '<(version_full)',
        'java_in_dir': '.',
        'input_jars_paths': ['./lib/ambientsdk.jar',],
        'resource_dir': '../browser/res',
         #TODO need proguard config file before we can enable this
        'proguard_enabled': 'false',
        'conditions': [
          ['icu_use_data_file_flag==1', {
            'additional_input_paths': [
              '<(PRODUCT_DIR)/icudtl.dat',
            ],
          }],
        ],
        'native_lib_target': 'libswe',
        'app_manifest_version_code': '<(swe_app_manifest_version_code)',
        'app_manifest_version_name': '<(swe_app_manifest_version_name)',

        'additional_native_libs': [
          '<@(swe_additional_native_libs)',
        ],
        'additional_input_paths': [
          '<@(chrome_android_pak_output_resources)',
        ],
        'res_extra_dirs': [ '<@(swe_extra_res_dirs)',
                          ],
        'R_package': '<(swe_browser_java_package)',
        'android_manifest_path': '<(SHARED_INTERMEDIATE_DIR)/swe_android_browser_apk/AndroidManifest.xml',
      },

      'copies': [
        {
          'destination': '<(PRODUCT_DIR)/swe_android_browser_apk/assets/',
          'files': [
            '<@(swe_assets)',
          ],
        },
        {
          'destination': '<(PRODUCT_DIR)/swe_android_browser_apk/assets/wml',
          'files': [
            '<@(swe_assets_wml)',
          ],
        },
      ],
      'includes': [ '../../build/java_apk.gypi' ],
    },
    #generate swe_channel.py for telemetry support
    {
      'target_name': 'swe_channel_py',
      'type': 'none',
      'variables': {
        'manifest_input_path': '<(DEPTH)/swe/tools/swe_channel.py.jinja2',
        'manifest_output_path': '<(DEPTH)/swe/tools/swe_channel.py',
        'manifest_config_file_path': '<(browser_config_path)',
        'swe_manifest_package':  '',
      },
      'includes': [ '../swe_generate_manifest.gypi' ],
    },
    #generate AndroidManifest.xml
    {
      'target_name': 'swe_android_browser_apk_manifest',
      'type': 'none',
      'dependencies': [
        'swe_android_browser_apk_manifest_internal_tool',
        'swe_channel_py'
      ],
      'variables': {
        'manifest_input_path': '<(DEPTH)/swe/browser/AndroidManifest.xml.jinja2',
        'manifest_output_path': '<(SHARED_INTERMEDIATE_DIR)/swe_android_browser_apk/AndroidManifest.xml',
        'manifest_config_file_path': '<(browser_config_path)',
        'swe_manifest_package':  '',
      },
      'includes': [ '../swe_generate_manifest.gypi' ],
    },

    #generate AndroidManifest.xml for internal tool
    {
      'target_name': 'swe_android_browser_apk_manifest_internal_tool',
      'type': 'none',
      'variables': {
        'manifest_input_path': '<(DEPTH)/swe/browser/AndroidManifest.xml.jinja2',
        'manifest_output_path': '<(SHARED_INTERMEDIATE_DIR)/swe_android_browser_apk/as/AndroidManifest.xml',
        'manifest_config_file_path': '<(browser_config_path)',
        'swe_manifest_package':  '<(swe_browser_java_package)',
      },
      'includes': [ '../swe_generate_manifest.gypi' ],
    },

    {
      'target_name': 'swe_android_browser_apk_config',
      'type': 'none',
      'variables': {
        'template_input_path': '<(DEPTH)/swe/browser/template/com/android/browser/BrowserConfig.java.template',
        'template_output_path': '<(SHARED_INTERMEDIATE_DIR)/templates/<(_target_name)/com/android/browser/BrowserConfig.java',
        'template_config_file_path': '<(browser_config_path)',
      },
      'includes': [ '../swe_browser_config.gypi' ],
    },

    {
      'target_name': 'swe_android_browser_fake_apk',
      'type': 'none',
      'dependencies': [
        'swe_android_browser_apk',
      ],
      'includes': [ '../../build/apk_fake_jar.gypi' ],
    },

    {
      'target_name': 'swe_android_browser_tests_apk',
        'type': 'none',
        'dependencies': [
          'swe_android_browser_fake_apk',
          '../base/base.gyp:base_java_test_support',
          '../content/content_shell_and_tests.gyp:content_java_test_support',
          '../net/net.gyp:net_java_test_support',
        ],
        'variables': {
          'apk_name': 'SWEBrowserTests',
          'standalone_manifest_package_name': '<!(python <(swe_py_config) \
                                     -i <(browser_config_path) \
                                     -c PACKAGE_NAME)',
          'override_package_name': '<(standalone_manifest_package_name).tests',
          'android_manifest_path': './tests/AndroidManifest.xml',
          'java_in_dir': './tests/startup',
          'is_test_apk': 1,
          'test_suite_name': 'swe_android_browser_tests',
        },
        'includes': [ '../../build/java_apk.gypi' ],
    },
  ],
}
