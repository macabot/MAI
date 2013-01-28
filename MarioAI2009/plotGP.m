%Michael Cabot      6047262
%Richard Rozeboom   6173292

function plotGP(x, meanList, covarianceList)
    hold on;
    topCov = meanList+covarianceList;
    bottomCov = fliplr(meanList-covarianceList);
    y = [topCov, bottomCov];
    doubleX = [x, fliplr(x)];

    fill(doubleX, y, [7 7 7]/8);
    plot(x, meanList)
end
