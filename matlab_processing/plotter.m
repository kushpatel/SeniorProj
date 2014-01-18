clear all
clc

data = parseFile('Gyroscope_6_f.txt');
% 2 = x-axis, 3 = y-axis, 4 = z-axis
coord = 3;
%noise = parseFile('Gyroscope_bg_noise_30s.txt');
%filtered = data(:,:) - noise(:,:);

times = zeros(size(data, 2), 1);
for i = 1 : size(data, 2)
    times(i) = i;
end

% f threshold = 0.003, minpkdist = 20
% space threshold = 0.005, minpkdist = 20
threshold_value = 0.003;
minpkdist = 20;
[pks, locs] = findpeaks(data(coord,:), 'threshold', threshold_value, 'minpeakdistance', minpkdist);
pks
locs

% f envpkheight = 0.015, envpkdist = 20
% space envpkheight = 0.012, envpkdist = 20
envpkheight = 0.015;
envpkdist = 20;
upperEnvelope = imdilate(data(coord,:), true(1, 9));
lowerEnvelope = imerode(data(coord,:), true(1, 9));
envelopeSum = upperEnvelope + abs(lowerEnvelope);
[pks, locs] = findpeaks(envelopeSum, 'minpeakdistance', envpkdist, 'minpeakheight', envpkheight);
pks
locs


figure
plot(times,data(coord,:),'b');
hold on;
plot(times,upperEnvelope, '-b');
plot(times,lowerEnvelope, '-b');