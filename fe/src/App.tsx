import { useEffect, useState } from "react";
import "./App.css";

function App() {
    const [stat, setStat] = useState<{
        totalCounts: number;
    }>({ totalCounts: 0 });
    useEffect(() => {
        // Dynamically load the Credly embed script
        const script = document.createElement("script");
        script.type = "text/javascript";
        script.src = "//cdn.credly.com/assets/utilities/embed.js";
        script.async = true;
        document.body.appendChild(script);

        // fetch the total counts from the api gateway
        (async () => {
            const response = await fetch("https://api.yuhd.online/");
            const data: { message: string; totalCounter: number } =
                await response.json();
            if (data.message === "Counter updated successfully") {
                setStat({ totalCounts: data.totalCounter });
            }
        })();
        return () => {
            // Cleanup the script if the component unmounts
            document.body.removeChild(script);
        };
    }, []);
    return (
        <div className="bg-white py-24 sm:py-32">
            <div className="mx-auto max-w-7xl px-6 lg:px-8">
                <dl className="grid grid-cols-1 gap-x-8 gap-y-16 text-center lg:grid-cols-1">
                    <div className="mx-auto flex max-w-xs flex-col gap-y-4">
                        <dt className="text-base/7 text-gray-600">
                            Total visitors
                        </dt>
                        <dd className="order-first text-3xl font-semibold tracking-tight text-gray-900 sm:text-5xl">
                            {stat?.totalCounts}
                        </dd>
                    </div>
                    <div className="mx-auto">
                        <div
                            data-iframe-width="250"
                            data-iframe-height="250"
                            data-share-badge-id="1b0cc6fe-6cbb-4e94-9242-91e811ed39cd"
                            data-share-badge-host="https://www.credly.com"
                        ></div>
                    </div>
                </dl>
            </div>
        </div>
    );
}

export default App;
