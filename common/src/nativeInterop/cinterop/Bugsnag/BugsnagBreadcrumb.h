#import <Foundation/Foundation.h>

typedef NS_ENUM(NSUInteger, BSGBreadcrumbType) {
/**
 *  Any breadcrumb sent via Bugsnag.leaveBreadcrumb()
 */
BSGBreadcrumbTypeManual,
/**
 *  A call to Bugsnag.notify() (internal use only)
 */
BSGBreadcrumbTypeError,
/**
 *  A log message
 */
BSGBreadcrumbTypeLog,
/**
 *  A navigation action, such as pushing a view controller or dismissing an alert
 */
BSGBreadcrumbTypeNavigation,
/**
 *  A background process, such performing a database query
 */
BSGBreadcrumbTypeProcess,
/**
 *  A network request
 */
BSGBreadcrumbTypeRequest,
/**
 *  Change in application or view state
 */
BSGBreadcrumbTypeState,
/**
 *  A user event, such as authentication or control events
 */
BSGBreadcrumbTypeUser,
};
