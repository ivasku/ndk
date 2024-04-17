#include <jni.h>
#include <string>
#include <jni.h>
#include <string>
#include <netinet/in.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <netdb.h>
#include <ifaddrs.h>
#include <android/log.h>


extern "C" JNIEXPORT jstring JNICALL
Java_com_example_test_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_test_MainActivity_getIPAddress(JNIEnv *env, jobject /* this */) {
    char hostBuffer[256];
    char *IPbuffer;
    struct hostent *host_entry;
    int hostname;

    // Retrieve the hostname
    hostname = gethostname(hostBuffer, sizeof(hostBuffer));
    if (hostname == -1) {
        return env->NewStringUTF("Failed to get hostname");
    }

    // Retrieve the host information
    host_entry = gethostbyname(hostBuffer);
    if (host_entry == NULL) {
        return env->NewStringUTF("Failed to get host entry");
    }

    // Convert the address to IPv4 IP address
    IPbuffer = inet_ntoa(*((struct in_addr*) host_entry->h_addr_list[0]));

    return env->NewStringUTF(IPbuffer);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_test_MainActivity_getExternalIPAddress(JNIEnv *env, jobject) {
    struct ifaddrs *ifaddr, *ifa;
    void *tmpAddrPtr = nullptr;

    std::string ipv6Address;
    std::string ipv4Address;
    std::string fallbackIpv4Address;

    if (getifaddrs(&ifaddr) == -1) {
        __android_log_print(ANDROID_LOG_ERROR, "IPFinder", "Failed to get ifaddrs");
        return env->NewStringUTF("Error fetching IP addresses");
    }

    for (ifa = ifaddr; ifa != nullptr; ifa = ifa->ifa_next) {
        if (!ifa->ifa_addr) continue;

        int family = ifa->ifa_addr->sa_family;
        if (family == AF_INET6) { // Check for IPv6 address
            tmpAddrPtr = &((struct sockaddr_in6 *)ifa->ifa_addr)->sin6_addr;
            char addressBuffer[INET6_ADDRSTRLEN];
            inet_ntop(AF_INET6, tmpAddrPtr, addressBuffer, INET6_ADDRSTRLEN);

            unsigned char firstByte = ((unsigned char*)tmpAddrPtr)[0];
            if (firstByte >= 0x20 && firstByte <= 0x3F) { // Global unicast (simplified check)
                ipv6Address = addressBuffer;
            }
        } else if (family == AF_INET) { // Check for IPv4 address
            tmpAddrPtr = &((struct sockaddr_in *)ifa->ifa_addr)->sin_addr;
            char addressBuffer[INET_ADDRSTRLEN];
            inet_ntop(AF_INET, tmpAddrPtr, addressBuffer, INET_ADDRSTRLEN);
            if (addressBuffer != std::string("127.0.0.1")) { // Exclude loopback
                if (((((struct sockaddr_in *)ifa->ifa_addr)->sin_addr.s_addr & 0xFF000000) != 0x0A000000) &&  // not 10.x.x.x
                    ((((struct sockaddr_in *)ifa->ifa_addr)->sin_addr.s_addr & 0xFFF00000) != 0xAC100000) &&  // not 172.16.0.0-172.31.255.255
                    ((((struct sockaddr_in *)ifa->ifa_addr)->sin_addr.s_addr & 0xFFFF0000) != 0xC0A80000)) {  // not 192.168.x.x
                    ipv4Address = addressBuffer;
                } else {
                    fallbackIpv4Address = addressBuffer; // Save as fallback non-loopback
                }
            }
        }
    }

    freeifaddrs(ifaddr);

    // Selecting the IP address based on priority
    if (!ipv6Address.empty()) {
        return env->NewStringUTF(ipv6Address.c_str());
    } else if (!ipv4Address.empty()) {
        return env->NewStringUTF(ipv4Address.c_str());
    } else if (!fallbackIpv4Address.empty()) {
        return env->NewStringUTF(fallbackIpv4Address.c_str());
    } else {
        return env->NewStringUTF("No suitable IP found");
    }


}