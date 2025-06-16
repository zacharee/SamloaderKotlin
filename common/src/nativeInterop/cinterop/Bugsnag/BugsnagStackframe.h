// The following are snippets from the Bugsnag Cocoa SDK used to generate Kotlin stubs.
//
// https://github.com/bugsnag/bugsnag-cocoa/blob/bd0465cd0e753ca42eef59fef4d5ceda80da1222/Bugsnag/include/Bugsnag/BugsnagStackframe.h
//
// Copyright (c) 2012 Bugsnag, https://bugsnag.com/
//
// Permission is hereby granted, free of charge, to any person obtaining
// a copy of this software and associated documentation files (the "Software"),
// to deal in the Software without restriction, including without limitation
// the rights to use, copy, modify, merge, publish, distribute, sublicense,
// and/or sell copies of the Software, and to permit persons to whom the Software
// is furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.

#import <Foundation/Foundation.h>

@interface BugsnagStackframe : NSObject

+ (NSArray<BugsnagStackframe *> *_Nonnull)stackframesWithCallStackReturnAddresses:(NSArray<NSNumber *> *_Nonnull)callStackReturnAddresses;

@end