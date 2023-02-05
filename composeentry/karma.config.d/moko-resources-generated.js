// workaround from https://github.com/ryanclark/karma-webpack/issues/498#issuecomment-790040818

const output = {
  path: require("os").tmpdir() + '/' + '_karma_webpack_' + Math.floor(Math.random() * 1000000),
}

config.set(
    {
        webpack: {... createWebpackConfig(), output},
        files: config.files.concat([{
                pattern: `${output.path}/**/*`,
                watched: false,
                included: false,
            }]
        )
    }
)