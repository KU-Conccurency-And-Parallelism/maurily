CXX := clang++
CXXFLAGS := -std=c++17 -O1 -pthread
TARGET := main
SRC := main.cpp

all: $(TARGET)

$(TARGET): $(SRC)
	$(CXX) $(CXXFLAGS) -o $@ $^

clean:
	rm -f $(TARGET)
