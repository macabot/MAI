function displayMeans(allData, means)

% plot all data with means
figure(1);
plot(allData(:,1), allData(:,2), '.');
hold on;
plot(means(:,1), means(:,2), '.r');
% plot all data with voronoi
figure(2);
plot(allData(:,1), allData(:,2), '.');
hold on;
voronoi(means(:,1), means(:,2), '.r');

