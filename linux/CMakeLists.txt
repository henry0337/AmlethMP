cmake_minimum_required(VERSION 3.21)

project(AmlethMP VERSION 0.0.1 LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

find_package(Qt6 6.4 REQUIRED COMPONENTS Quick QuickControls2)

qt_standard_project_setup()

qt_add_executable(amlethmp src/main.cpp)

qt_add_qml_module(amlethmp
    URI AmlethMP
    VERSION 1.0
    RESOURCE_PREFIX /qt/qml
    QML_FILES qml/Main.qml
)

target_link_libraries(amlethmp PRIVATE
    Qt6::Quick
    Qt6::QuickControls2
)

include(GNUInstallDirs)
install(TARGETS amlethmp RUNTIME DESTINATION ${CMAKE_INSTALL_BINDIR})
