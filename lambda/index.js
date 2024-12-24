const {
    DynamoDBClient,
    UpdateItemCommand,
} = require("@aws-sdk/client-dynamodb");

const dynamoDBClient = new DynamoDBClient();
const tableName = process.env.TABLE_NAME;

exports.handler = async (event) => {
    const id = "1"; // ID of the item to update

    try {
        const params = {
            TableName: tableName,
            Key: {
                id: { S: id },
            },
            UpdateExpression:
                "SET user_counter = if_not_exists(user_counter, :start) + :increment",
            ExpressionAttributeValues: {
                ":start": { N: "0" }, // Initialize counter to 0 if it doesn't exist
                ":increment": { N: "1" }, // Increment by 1
            },
            ReturnValues: "UPDATED_NEW",
        };

        const command = new UpdateItemCommand(params);
        const result = await dynamoDBClient.send(command);

        return {
            statusCode: 200,
            body: JSON.stringify({
                message: "Counter updated successfully",
                totalCounter: result.Attributes.user_counter.N, // Extract updated counter value
            }),
        };
    } catch (error) {
        console.error("Error updating the user counter:", error);
        return {
            statusCode: 500,
            body: JSON.stringify({
                message: "Internal Server Error",
                error: error.message,
            }),
        };
    }
};
