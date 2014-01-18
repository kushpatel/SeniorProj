function [ data ] = parseFile(filename)
%Parser parses out the file with the filename given
%returns arrays filled with data from file

% Count the number of lines (coordinates) for proper array allocation
filelines = fopen(filename);
line = fgetl(filelines);
numlines = 1;
while ischar(line)
    line = fgetl(filelines);
    numlines = numlines + 1;
end

%Arrays space allocation
data = zeros(4,numlines - 1);

% Populate arrays with data from file
file = fopen(filename);
line = fgetl(file);
i = 1;
%line = '496642211916, 0.920286, -3.448229, -6.630013';
while ischar(line)
    splitline = regexp(line,', ','split');
    data(1,i) = str2double(splitline(1));
    data(2,i) = str2double(splitline(2));
    data(3,i) = str2double(splitline(3));
    data(4,i) = str2double(splitline(4));
    line = fgetl(file);
    i = i + 1;
end

end

